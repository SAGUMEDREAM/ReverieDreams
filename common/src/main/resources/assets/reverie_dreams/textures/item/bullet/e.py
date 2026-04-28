import os

def rename_png_files_in_folders():
    # 获取当前目录下所有文件夹
    for root, dirs, files in os.walk('.'):
        # 只处理文件夹中的 png 文件
        if not dirs:  # 排除文件夹中的子文件夹
            png_files = [f for f in files if f.endswith('.png')]
            png_files.sort()  # 按照文件名默认排序

            # 遍历每个 PNG 文件，重新命名
            for idx, png_file in enumerate(png_files):
                old_path = os.path.join(root, png_file)
                new_name = f"{idx}.png"
                new_path = os.path.join(root, new_name)

                # 重命名文件
                os.rename(old_path, new_path)
                print(f"已将 {old_path} 重命名为 {new_path}")

if __name__ == "__main__":
    rename_png_files_in_folders()
