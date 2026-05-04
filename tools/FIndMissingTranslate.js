const fs = require("fs");
const path = require("path");
const importA = "D:\\Gradle\\ReverieDreams\\common\\src\\generated\\resources\\assets\\reverie_dreams\\lang\\zh_cn.json";
const importB = "D:\\Gradle\\ReverieDreams\\common\\src\\main\\resources\\assets\\reverie_dreams\\lang\\en_us.json";
const exportFileName = "missing_in_en_us.json";

function main() {
    const zhData = loadFile(importA);
    const enData = loadFile(importB);

    if (!zhData || !enData) {
        console.error("加载文件失败，退出");
        return;
    }

    const missingKeys = {};
    for (const key of Object.keys(zhData)) {
        if (!(key in enData)) {
            missingKeys[key] = zhData[key];
        }
    }

    if (Object.keys(missingKeys).length === 0) {
        console.log("没有缺失的键");
    } else {
        saveFile(missingKeys, exportFileName);
    }
}

function saveFile(data, fileName = exportFileName) {
    try {
        const jsonStr = JSON.stringify(data, null, 2);
        fs.writeFileSync(path.resolve(fileName), jsonStr, "utf-8");
        console.log(`文件保存成功: ${fileName}`);
    } catch (e) {
        console.error("保存文件失败:", e.message);
    }
}

function loadFile(filePath) {
    try {
        const absPath = path.resolve(filePath);
        const content = fs.readFileSync(absPath, "utf-8");
        return JSON.parse(content);
    } catch (e) {
        console.error("读取文件失败:", e.message);
        return null;
    }
}

main();