import os
from PIL import Image

def overlay_layers(image, num_overlays=5):
    """
    将原图层叠加多次
    """
    # 创建一个空白图像，与原图大小相同
    result = Image.new("RGBA", image.size, (0, 0, 0, 0))
    
    # 叠加原图层多次
    for _ in range(num_overlays):
        result = Image.alpha_composite(result, image)
    
    return result

def process_image(image_path):
    # 打开图片并确保是RGBA模式（带有透明度）
    img = Image.open(image_path).convert("RGBA")
    
    # 叠加图层
    processed_img = overlay_layers(img, num_overlays=5)
    
    # 直接覆盖原文件
    processed_img.save(image_path, "PNG")
    print(f"Processed and overwritten: {image_path}")

def scan_and_process_folder(root_folder):
    for foldername, subfolders, filenames in os.walk(root_folder):
        for filename in filenames:
            if filename.lower().endswith('.png'):
                image_path = os.path.join(foldername, filename)
                process_image(image_path)

if __name__ == "__main__":
    # 当前文件夹
    current_folder = os.getcwd()
    
    # 扫描并处理图片
    scan_and_process_folder(current_folder)
    
    print("All images processed and overwritten!")