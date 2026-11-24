package cc.thonly.reverie_dreams.client.audio.radio;

import cc.thonly.reverie_dreams.block.decor.RecordPlayerMedia;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import static org.junit.jupiter.api.Assertions.*;

class RadioTest {
    @Test void onlyHttpAndHttpsAreAcceptedAndGroupingPreservesPathAndQuery() {
        assertEquals("https://example.com/a%20b.mp3?token=a%2Fb",RecordPlayerMedia.normalize(" HTTPS://EXAMPLE.COM:443/a%20b.mp3?token=a%2Fb#ignored "));
        assertEquals("http://example.com/",RecordPlayerMedia.normalize("http://EXAMPLE.com:80"));
        assertEquals("http://[::1]:8080/a",RecordPlayerMedia.normalize("http://[::1]:8080/a"));
        for(String url:List.of("file:///C:/music.mp3","FILE:///etc/passwd","jar:file:/x","data:audio/mpeg;base64,AAA","ftp://example.com/x","C:/x.mp3","//example.com/x","https:///x","http://user:pass@example.com/a","http://example.com:0/","http://example.com:70000/","https://example.com/\nfile"))assertThrows(IllegalArgumentException.class,()->RecordPlayerMedia.normalize(url),url);
        assertThrows(IllegalArgumentException.class,()->RecordPlayerMedia.normalize("https://example.com/"+"a".repeat(2048)));
        assertNotEquals(RecordPlayerMedia.normalize("https://example.com/a?q=1"),RecordPlayerMedia.normalize("https://example.com/a?q=2"));
    }
    @Test void redirectToLocalFileIsRejectedAndRedirectsAreBounded() throws Exception {
        var requests=new AtomicInteger();var server=HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(),0),0);
        server.createContext("/local",e->{requests.incrementAndGet();e.getResponseHeaders().set("Location","file:///etc/passwd");e.sendResponseHeaders(302,-1);e.close();});
        server.createContext("/loop",e->{requests.incrementAndGet();e.getResponseHeaders().set("Location","/loop");e.sendResponseHeaders(302,-1);e.close();});server.start();
        var radio=new RadioStream(URI.create("http://localhost:"+server.getAddress().getPort()+"/local"),10000);
        try{await(()->radio.status()==RadioStream.Status.RECONNECTING,3000);assertEquals(1,requests.get());assertEquals(0,radio.framesDecoded());}finally{radio.close();}
        requests.set(0);var loop=new RadioStream(URI.create("http://localhost:"+server.getAddress().getPort()+"/loop"),10000);
        try{await(()->loop.status()==RadioStream.Status.RECONNECTING,3000);assertEquals(6,requests.get());}finally{loop.close();server.stop(0);}
    }
    @Test void finiteFileResumesAtConsumedPcmAndStopsAtEofWithoutReconnecting() throws Exception {
        byte[] mp3=fixture();var requests=new AtomicInteger();var server=HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(),0),0);
        server.createContext("/file",e->{try{requests.incrementAndGet();e.getResponseHeaders().set("Content-Type","audio/mpeg");e.sendResponseHeaders(200,mp3.length);e.getResponseBody().write(mp3);}finally{e.close();}});server.start();
        var url=URI.create("http://localhost:"+server.getAddress().getPort()+"/file");var first=new RadioStream(url,20);
        byte[] expected;
        try{await(()->first.framesDecoded()>20,5000);assertTrue(first.isFinite());first.read(8820);assertEquals(4410,first.positionSamples());var next=first.read(8820);expected=new byte[next.remaining()];next.get(expected);}finally{first.close();}
        var resumed=new RadioStream(url,20,4410);
        try{await(()->resumed.framesDecoded()>20,5000);assertEquals(4410,resumed.positionSamples());var next=resumed.read(8820);byte[] actual=new byte[next.remaining()];next.get(actual);assertArrayEquals(expected,actual,"Resume must skip already consumed PCM, not buffered decoder frames");
            long end=System.nanoTime()+TimeUnit.SECONDS.toNanos(5);while(!resumed.isFinished()&&System.nanoTime()<end){resumed.read(8820);Thread.sleep(2);}assertTrue(resumed.isFinished());await(()->!resumed.workerAlive(),2000);Thread.sleep(100);assertEquals(2,requests.get());assertEquals(0,resumed.read(8820).remaining());
        }finally{resumed.close();server.stop(0);}
    }
    @Test void chunkedLiveAndIcyResponsesNeverRestoreFileBookmarks() throws Exception {
        byte[] mp3=fixture();var server=HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(),0),0);
        for(String path:List.of("/chunked","/icy"))server.createContext(path,e->{try{e.getResponseHeaders().set("Content-Type","audio/mpeg");if(path.equals("/icy"))e.getResponseHeaders().set("icy-name","Live broadcast");e.sendResponseHeaders(200,path.equals("/icy")?mp3.length:0);e.getResponseBody().write(mp3);}finally{e.close();}});server.start();
        try{for(String path:List.of("/chunked","/icy")){var radio=new RadioStream(URI.create("http://localhost:"+server.getAddress().getPort()+path),20,5000000);try{await(()->radio.framesDecoded()>5,5000);assertFalse(radio.isFinite());radio.read(8820);assertEquals(0,radio.positionSamples());}finally{radio.close();}}}finally{server.stop(0);}
    }
    @Test void gainHasFiniteRangeMonotonicFalloffAndNoDuplicateAmplification() {
        assertEquals(1, RadioVolume.atDistance(0)); assertEquals(1, RadioVolume.atDistance(3));
        assertEquals(0, RadioVolume.atDistance(32)); assertEquals(0, RadioVolume.atDistance(10000));
        float previous=1;
        for(double d=0;d<=40;d+=.1){float next=RadioVolume.atDistance(d);assertTrue(next<=previous+.00001);assertTrue(next>=0&&next<=1);previous=next;}
        float combined=0;
        for(int i=0;i<10000;i++)combined=RadioVolume.merge(combined,.6f);
        assertEquals(.6f,combined); assertEquals(.9f,RadioVolume.merge(combined,.9f));
    }
    @Test void smoothGainConvergesWithoutOvershootWhenChangingNearestSpeaker() {
        float value=0;
        for(int i=0;i<60;i++){value=RadioVolume.smooth(value,.8f);assertTrue(value<=.8f);}
        assertEquals(.8f,value,.0001f);
        for(int i=0;i<60;i++){value=RadioVolume.smooth(value,0);assertTrue(value>=0);}
        assertTrue(value<.0001);
    }
    @Test void stereoDownmixAndRateConversionKeepDurationAndAmplitude() {
        var converter=new RadioPcm();short[] input=new short[480*2];
        for(int i=0;i<480;i++){input[2*i]=10000;input[2*i+1]=-2000;}
        short[] pcm=converter.convert(input,input.length,2,48000);
        assertEquals(441,pcm.length);for(short sample:pcm)assertEquals(4000,sample);
        assertEquals(441,converter.convert(input,input.length,2,48000).length);
        assertThrows(IllegalArgumentException.class,()->converter.convert(input,input.length,7,48000));
    }
    @Test void icyMetadataIsRemovedIncludingPartialReadsAndZeroLengthMetadata() throws Exception {
        var raw=new ByteArrayOutputStream();raw.write(new byte[]{1,2,3});raw.write(1);raw.write(new byte[16]);raw.write(new byte[]{4,5,6});raw.write(0);raw.write(new byte[]{7,8,9});
        try(var input=new IcyInputStream(new ByteArrayInputStream(raw.toByteArray()),3)){assertArrayEquals(new byte[]{1,2,3,4,5,6,7,8,9},input.readAllBytes());}
        try(var input=new IcyInputStream(new ByteArrayInputStream(new byte[]{1,2,3,1}),3)){assertThrows(IOException.class,input::readAllBytes);}
    }
    @Test void retryIsBoundedAndDoesNotBusyLoopOnLongOutage() {
        assertEquals(2000,RadioStream.retryDelay(2000,1));assertEquals(4000,RadioStream.retryDelay(2000,2));assertEquals(60000,RadioStream.retryDelay(2000,6));assertEquals(60000,RadioStream.retryDelay(2000,100));
    }
    @Test void persistentOutageReachesTheFullBackoffWithoutSpawningExtraStreams() throws Exception {
        var attempts=new CopyOnWriteArrayList<Long>();var server=HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(),0),0);
        server.createContext("/offline",exchange->{try{attempts.add(System.nanoTime());exchange.sendResponseHeaders(503,1);exchange.getResponseBody().write(0);}finally{exchange.close();}});server.start();
        var radio=new RadioStream(URI.create("http://localhost:"+server.getAddress().getPort()+"/offline"),20);
        try{await(()->attempts.size()>=7,6000);assertTrue(attempts.get(6)-attempts.get(5)>=TimeUnit.MILLISECONDS.toNanos(500),"Six consecutive failures must reach the final backoff tier");assertEquals(0,radio.framesDecoded());assertEquals(8820,radio.read(8820).remaining());}
        finally{radio.close();server.stop(0);}await(()->!radio.workerAlive(),2000);
    }
    static byte[] fixture() throws IOException {return Files.readAllBytes(Path.of(System.getProperty("reverie.radio.fixture")));}
    static void await(BooleanSupplier condition,long millis) throws Exception {long end=System.nanoTime()+TimeUnit.MILLISECONDS.toNanos(millis);while(!condition.getAsBoolean()&&System.nanoTime()<end)Thread.sleep(10);assertTrue(condition.getAsBoolean(),"Timed out");}
    @Test void failedHttpResponseReconnectsAndRealMp3DecodesToMonoPcm() throws Exception {
        byte[] mp3=fixture();var requests=new AtomicInteger();var server=HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(),0),0);
        server.createContext("/radio", exchange->{try{int count=requests.incrementAndGet();assertEquals("0",exchange.getRequestHeaders().getFirst("Icy-MetaData"));exchange.getResponseHeaders().set("Content-Type",count==1?"text/plain":"audio/mpeg");exchange.sendResponseHeaders(count==1?503:200,count==1?1:mp3.length);exchange.getResponseBody().write(count==1?new byte[]{0}:mp3);}finally{exchange.close();}});server.start();
        RadioStream radio=new RadioStream(URI.create("http://localhost:"+server.getAddress().getPort()+"/radio"),20);
        try {
            await(()->radio.framesDecoded()>4,5000);assertTrue(requests.get()>=2);assertEquals(1,radio.getFormat().getChannels());assertEquals(44100,radio.getFormat().getSampleRate());
            var bytes=radio.read(8820);assertEquals(8820,bytes.remaining());boolean nonzero=false;while(bytes.hasRemaining())if(bytes.get()!=0)nonzero=true;assertTrue(nonzero,"Actual MPEG samples must reach the Minecraft stream");
        } finally {radio.close();server.stop(0);}
        await(()->!radio.workerAlive(),2000);assertTrue(radio.isClosed());assertEquals(0,radio.read(8820).remaining());
    }
    @Test void stalledNetworkNeverBlocksAudioReadsAndCloseCancelsTheDecoder() throws Exception {
        var sent=new CountDownLatch(1);var release=new CountDownLatch(1);var server=HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(),0),0);
        server.createContext("/stall",exchange->{try{exchange.sendResponseHeaders(200,0);exchange.getResponseBody().flush();sent.countDown();release.await(3,TimeUnit.SECONDS);}catch(InterruptedException error){Thread.currentThread().interrupt();}finally{exchange.close();}});server.start();
        var radio=new RadioStream(URI.create("http://localhost:"+server.getAddress().getPort()+"/stall"),20);
        try {assertTrue(sent.await(3,TimeUnit.SECONDS));long start=System.nanoTime();var silence=radio.read(8820);assertTrue(System.nanoTime()-start<TimeUnit.MILLISECONDS.toNanos(100));assertEquals(8820,silence.remaining());while(silence.hasRemaining())assertEquals(0,silence.get());start=System.nanoTime();radio.close();assertTrue(System.nanoTime()-start<TimeUnit.MILLISECONDS.toNanos(500));await(()->!radio.workerAlive(),2000);}
        finally{radio.close();release.countDown();server.stop(0);}
    }
}
