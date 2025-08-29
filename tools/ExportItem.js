const fs = require("fs");
const path = require("path");
const base = "drink";
const dirPath = "D:\\Gradle\\PolymeredTouhouMod\\src\\main\\generated\\assets\\reverie_dreams\\items\\drink";
const outputFile = "./test.java";

function main() {
    const files = fs.readdirSync(dirPath);
    let javaSrc = "";
    files.forEach(file => {
        let s = process(file.replaceAll(".json", "")) + "\n";
        javaSrc += s;
    });
    fs.writeFileSync(outputFile, javaSrc, "utf8");
}

function process(itemId) {
    let str = "";
    let field = itemId.toUpperCase();
    str += `public static final Item ${field} = registerDrinkItem("${base}/${itemId}", DrinkItem::new, new Item.Settings());`;
    return str;
}

main();