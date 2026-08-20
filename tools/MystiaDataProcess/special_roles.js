import fs from "fs";

const input = fs.readFileSync("./OrderLang.log", "utf8");

const results = [];

for (const line of input.split(/\r?\n/)) {
    const match = line.match(
        /^Order Lang:\s*SpecialGuest=(\d+),\s*TagId=(\d+),\s*Request=(.*)$/
    );

    if (!match) {
        continue;
    }

    const [, specialGuest, tagId, request] = match;

    results.push({
        SpecialGuest: Number(specialGuest),
        TagId: Number(tagId),
        Request: request
    });
}

// CSV 转义
function escapeCsv(value) {
    const text = String(value ?? "");

    // CSV 中包含逗号、双引号、换行时，需要用双引号包起来
    if (/[",\r\n]/.test(text)) {
        return `"${text.replaceAll('"', '""')}"`;
    }

    return text;
}

const csv = [
    "SpecialGuest,TagId,Request",
    ...results.map(item =>
        [
            item.SpecialGuest,
            item.TagId,
            escapeCsv(item.Request)
        ].join(",")
    )
].join("\n");

// 写入 CSV
fs.writeFileSync("./OrderLang.csv", "\uFEFF" + csv, "utf8");

console.log(`生成完成，共 ${results.length} 条`);