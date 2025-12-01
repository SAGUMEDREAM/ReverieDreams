#!/usr/bin/env python3
"""
Minecraft 唱片 WAV -> 单声道 OGG 转换脚本
要求:
- 输入 WAV 文件
- 输出单声道 OGG（保留左右声道混合音，避免相位抵消）
- 采样率 44.1 kHz
- 音质 qscale 5 (≈160 kbps)
"""

import subprocess
import sys
from pathlib import Path

def convert_wav_to_minecraft_ogg(input_path: str, output_path: str = None):
    input_file = Path(input_path)
    if not input_file.exists():
        print(f"错误: 输入文件不存在: {input_path}")
        return

    if output_path is None:
        output_file = input_file.with_suffix(".ogg")
    else:
        output_file = Path(output_path)

    # FFmpeg 命令
    ffmpeg_cmd = [
        "ffmpeg",
        "-y",  # 自动覆盖输出文件
        "-i", str(input_file),
        "-ac", "1",  # 单声道
        "-ar", "44100",  # 采样率 44.1 kHz
        "-af", "pan=mono|c0=0.5*FL+0.5*FR",  # 避免相位抵消
        "-c:a", "libvorbis",
        "-qscale:a", "5",  # 音质
        str(output_file)
    ]

    print("正在转换...")
    subprocess.run(ffmpeg_cmd, check=True)
    print(f"完成! 输出文件: {output_file}")

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("用法: python3 convert_to_mc_ogg.py 输入文件.wav [输出文件.ogg]")
    else:
        input_file = sys.argv[1]
        output_file = sys.argv[2] if len(sys.argv) > 2 else None
        convert_wav_to_minecraft_ogg(input_file, output_file)
