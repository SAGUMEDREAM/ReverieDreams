const fs = require("fs");

const fileName = "D:\\Gradle\\ReverieDreams\\common\\src\\main\\java\\cc\\thonly\\reverie_dreams\\registry\\content\\item\\RDIngredientItems.java";

// 读取 Java 文件
const content = fs.readFileSync(fileName, "utf-8");

// 提取 food/xxx 中的 xxx
const regex = /"ingredient\/([a-z0-9_]+)"/gi;

const result = [];
let match;

while ((match = regex.exec(content)) !== null) {
    result.push(match[1]);
}

// 输出到当前目录 JSON 文件
const outputPath = "./ingredient_items.json";

fs.writeFileSync(
    outputPath,
    JSON.stringify(result, null, 2),
    "utf-8"
);

console.log("已导出到:", outputPath);