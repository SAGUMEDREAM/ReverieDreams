// noinspection JSUnresolvedReference,SpellCheckingInspection,EqualityComparisonWithCoercionJS

import fs from 'fs';
import {parse} from 'csv-parse/sync';

function readCsv(path) {
    const csv = fs.readFileSync(path, 'utf8');

    return parse(csv, {
        columns: true,
        skip_empty_lines: true,
        bom: true,
    });
}

function buildList(items, dataMap) {
    return items
        .map(item => dataMap.get(item))
        .filter(Boolean);
}

function buildListStr(items, dataMap, indent = 12) {
    const fields = buildList(items, dataMap);

    if (fields.length === 0) {
        return "List.of()";
    }

    return `List.of(\n${fields
        .map(field => `${" ".repeat(indent)}${field}`)
        .join(",\n")}\n${" ".repeat(indent - 4)})`;
}

function buildItemDescriptionRegisterCode(fieldname, fieldKey, description) {
    const parts = String(description ?? "")
        .split(/[，。]/)
        .map(text => text.trim())
        .filter(Boolean);

    if (parts.length === 0) {
        return "";
    }

    const constNames = parts.map(
        (_, index) =>
            `ItemDescriptionConsts.${fieldKey}_DESCRIPTION_${index}`
    );

    return `addItem(
        ${fieldname}.asItem(),
        List.of(
            ${constNames.join(",\n            ")}
        )
);
`;
}

function buildListString(items, indent = 12) {
    if (items.length === 0) {
        return "List.of()";
    }

    return `List.of(\n${items
        .map(field => `${" ".repeat(indent)}${field}`)
        .join(",\n")}\n${" ".repeat(indent - 4)})`;
}

function buildDescriptionCode(fieldKey, description) {
    const parts = String(description ?? "")
        .split(/[，。]/)
        .map(text => text.trim())
        .filter(Boolean);

    let code = "";

    for (let i = 0; i < parts.length; i++) {
        const constName = `${fieldKey}_DESCRIPTION_${i}`;

        code +=
            `translationBuilder.add(ItemDescriptionConsts.${constName}, ${JSON.stringify(parts[i])});\n`;
    }

    return code;
}

function buildDescriptionStaticCode(fieldKey, description) {
    const parts = String(description ?? "")
        .split(/[，。]/)
        .map(text => text.trim())
        .filter(Boolean);

    let code = "";

    for (let i = 0; i < parts.length; i++) {
        const constName = `${fieldKey}_DESCRIPTION_${i}`;

        const translationKey =
            `reverie_dreams.item.${fieldKey.toLowerCase()}.description.${i}`;

        code +=
            `public static final String ${constName} = "${translationKey}";\n`;
    }

    return code;
}

function escapeJavaString(str) {
    return String(str)
        .replace(/\\/g, "\\\\")
        .replace(/"/g, '\\"')
        .replace(/\r/g, "\\r")
        .replace(/\n/g, "\\n");
}


/**
 * 从角色字段名中获取纯角色名
 *
 * NPCRoles.WRIGGLE_NIGHTBUG
 * ->
 * WRIGGLE_NIGHTBUG
 */
function getRoleName(roleFieldName) {
    return roleFieldName
        .replace("NPCRoles.", "")
        .toUpperCase();
}


/**
 * 根据 SpecialGuest 获取订单
 */
function findOrders(id) {
    const result = [];

    for (const element of orderLangData.values()) {
        if (Number(element.id) === Number(id)) {
            result.push(element);
        }
    }

    return result;
}


/**
 * 生成订单相关代码
 *
 * 返回：
 * {
 *     constNames,
 *     langCode,
 *     staticCode
 * }
 */
function buildOrderCode(roleName, specialGuest) {
    const orders = findOrders(specialGuest);

    let langCode = "";
    let staticCode = "";

    const constNames = [];

    let orderIdx = 0;

    for (const order of orders) {
        const request = order.request;

        if (request == null || request === "") {
            continue;
        }

        const constName =
            `CUSTOMER_ORDER_${roleName}_${orderIdx}`;

        const translationKey =
            `reverie_dreams.role.${roleName.toLowerCase()}.order.${orderIdx}`;

        // =========================
        // LanguageProvider
        // =========================

        langCode +=
            `translationBuilder.add(CustomerOrderConsts.${constName}, ${JSON.stringify(request)});\n`;

        // =========================
        // CustomerOrderConsts
        // =========================

        staticCode +=
            `public static final String ${constName} = "${translationKey}";\n`;

        constNames.push(
            `CustomerOrderConsts.${constName}`
        );

        orderIdx++;
    }

    return {
        constNames,
        langCode,
        staticCode
    };
}

function buildEvaluationConstructorCode(roleName, evaluation) {
    const fields = [
        "exbad",
        "bad",
        "norm",
        "good",
        "exgood",
        "lackmoneyangry",
        "lackmoneynormal",
        "repell",
        "seenRepell"
    ];

    if (!evaluation) {
        return `new CustomerEvaluation()`;
    }

    const values = fields.map(field => {
        const value = evaluation[field];

        if (value == null || value === "") {
            return "null";
        }

        return `CustomerEvaluationConsts.CUSTOMER_${roleName}_EVALUATION_${field.toUpperCase()}`;
    });

    return `new CustomerEvaluation(\n${values
        .map(value => `                ${value}`)
        .join(",\n")}\n        )`;
}

function buildEvaluationCode(roleName, evaluation) {
    if (!evaluation) {
        return {
            constNames: [],
            langCode: "",
            staticCode: ""
        };
    }

    const fields = [
        "exbad",
        "bad",
        "norm",
        "good",
        "exgood",
        "lackmoneyangry",
        "lackmoneynormal",
        "repell",
        "seenRepell"
    ];

    const constNames = [];
    let langCode = "";
    let staticCode = "";

    for (const field of fields) {
        const value = evaluation[field];

        if (value == null || value === "") {
            continue;
        }

        const upperField = field.toUpperCase();

        const constName =
            `CUSTOMER_${roleName}_EVALUATION_${upperField}`;

        const translationKey =
            `reverie_dreams.role.${roleName.toLowerCase()}.evaluation.${field}`;

        langCode +=
            `translationBuilder.add(CustomerEvaluationConsts.${constName}, "${escapeJavaString(value)}");\n`;

        staticCode +=
            `public static final String ${constName} = "${translationKey}";\n`;

        constNames.push({
            field,
            constName: `CustomerEvaluationConsts.${constName}`
        });
    }

    return {
        constNames,
        langCode,
        staticCode
    };
}


const mystia_customer_data = JSON.parse(
    fs.readFileSync("./customer_data.json", 'utf8')
);

const mystia_food_data = JSON.parse(
    fs.readFileSync("./food.json", 'utf8')
);

const mystia_ingredients_data = JSON.parse(
    fs.readFileSync("./ingredients.json", 'utf8')
);

const mysita_beverage_data = JSON.parse(
    fs.readFileSync("./beverages.json", 'utf8')
);


const customerData = new Map();
const foodTagData = new Map();
const beverageData = new Map();
const itemData = new Map();
const orderLangData = new Map();


const customerDataArray = readCsv("./CustomerData.csv");
const foodTagDataArray = readCsv("./FoodTags.csv");
const beverageDataArray = readCsv("./BeverageTags.csv");
const itemDataArray = readCsv("./Item.csv");
const orderLangArray = readCsv("./OrderLang.csv");


for (const element of customerDataArray) {
    customerData.set(
        element["Name"],
        element["FieldName"]
    );
}

for (const element of foodTagDataArray) {
    foodTagData.set(
        element["Name"],
        element["FieldName"]
    );
}

for (const element of beverageDataArray) {
    beverageData.set(
        element["Name"],
        element["FieldName"]
    );
}

for (const element of itemDataArray) {
    itemData.set(
        element["Name"],
        element["FieldName"]
    );
}


/**
 * OrderLang.csv
 *
 * SpecialGuest,TagId,Request
 */
for (const element of orderLangArray) {
    orderLangData.set(
        orderLangData.size,
        {
            id: Number(element["SpecialGuest"]),
            tag_id: Number(element["TagId"]),
            request: element["Request"]
        }
    );
}


async function main() {

    let providerCode = "";

    let chatCode = "";
    let descriptionCode = "";

    let chatStaticCode = "";
    let descriptionStaticCode = "";

    let itemDescriptionCode = "";
    let itemDescriptionRegisterCode = "";
    let itemDescriptionStaticCode = "";

    // =========================
    // Order
    // =========================

    let orderChatCode = "";
    let orderChatStaticCode = "";
    let orderChatLangProviderCode = "";


    // =========================================================
    // Customer
    // =========================================================

    let evaluationCode = "";
    let evaluationStaticCode = "";

    for (const data of mystia_customer_data) {

        const roleFieldName =
            customerData.get(data.name);

        if (!roleFieldName) {
            console.warn(`找不到角色: ${data.name}`);
            continue;
        }

        const roleName =
            getRoleName(roleFieldName);


        const description =
            data.description ?? [];

        const chat =
            data.chat ?? [];


        let chatIdx = 0;
        let descriptionIdx = 0;


        // =====================================================
        // Chat
        // =====================================================

        const chatConstNames = [];

        for (const element of chat) {

            if (element == null) {
                continue;
            }

            const translateKey =
                `reverie_dreams.role.${roleName}.chat.${chatIdx}`
                    .toLowerCase();

            const constName =
                `ROLE_${roleName}_CHAT_${chatIdx}`;


            chatCode +=
                `translationBuilder.add(RoleChatConsts.${constName}, "${escapeJavaString(element)}");\n`;


            chatStaticCode +=
                `public static final String ${constName} = "${translateKey}";\n`;


            chatConstNames.push(
                `RoleChatConsts.${constName}`
            );

            chatIdx++;
        }


        // =====================================================
        // Description
        // =====================================================

        const descriptionConstNames = [];

        for (const element of description) {

            if (element == null) {
                continue;
            }

            const translateKey =
                `reverie_dreams.role.${roleName}.description.${descriptionIdx}`
                    .toLowerCase();

            const constName =
                `ROLE_${roleName}_DESCRIPTION_${descriptionIdx}`;


            descriptionCode +=
                `translationBuilder.add(RoleDescriptionConsts.${constName}, "${escapeJavaString(element)}");\n`;


            descriptionStaticCode +=
                `public static final String ${constName} = "${translateKey}";\n`;


            descriptionConstNames.push(
                `RoleDescriptionConsts.${constName}`
            );

            descriptionIdx++;
        }


        // =====================================================
        // Order
        // =====================================================

        const orderResult =
            buildOrderCode(
                roleName,
                data.id
            );

        orderChatCode +=
            orderResult.langCode;

        orderChatStaticCode +=
            orderResult.staticCode;


        const orders =
            buildListString(
                orderResult.constNames
            );


        // =====================================================
        // Food / Beverage
        // =====================================================

        const likes =
            buildListStr(
                data.positiveTags ?? [],
                foodTagData
            );

        const dislikes =
            buildListStr(
                data.negativeTags ?? [],
                foodTagData
            );

        const beverages =
            buildListStr(
                data.beverageTags ?? [],
                beverageData
            );


        // =====================================================
        // Chat / Description
        // =====================================================

        const chats =
            buildListString(
                chatConstNames
            );

        const descriptions =
            buildListString(
                descriptionConstNames
            );

        // =====================================================
        // Evaluation
        // =====================================================

        const evaluationResult =
            buildEvaluationCode(
                roleName,
                data.evaluation
            );

        evaluationCode +=
            evaluationResult.langCode;

        evaluationStaticCode +=
            evaluationResult.staticCode;

        const evaluation =
            buildEvaluationConstructorCode(
                roleName,
                data.evaluation
            );


        // =====================================================
        // addRare
        // =====================================================

        providerCode +=
            `this.addRare(${roleFieldName},\n`;

        providerCode +=
            `        new CustomerBudget(${data.price[0]}, ${data.price[1]}),\n`;

        providerCode +=
            `        ${likes},\n`;

        providerCode +=
            `        ${dislikes},\n`;

        providerCode +=
            `        ${beverages},\n`;

        providerCode +=
            `        ${chats},\n`;

        providerCode +=
            `        ${orders},\n`;

        providerCode +=
            `        ${descriptions},\n`;

        providerCode +=
            `        ${evaluation}\n`;

        providerCode +=
            `);\n\n`;
    }


    // =========================================================
    // Item Description
    // =========================================================

    const itemSources = [
        mystia_food_data,
        mystia_ingredients_data,
        mysita_beverage_data
    ];


    for (const source of itemSources) {

        for (const data of source) {

            const itemname =
                data.name;

            const fieldname =
                itemData.get(itemname);


            if (fieldname == null) {
                console.log(`不存在 ${itemname}`);
                continue;
            }


            const fieldKey =
                fieldname
                    .replace("RDFoodItems.", "")
                    .replace("RDIngredientItems.", "")
                    .replace("RDBlocks.", "")
                    .replace("RDItems.", "")
                    .replace("RDBeverageItems.", "")
                    .replace("Items.", "")
                    .toUpperCase();


            const description =
                data.description ?? "";


            // =================================================
            // 注册 Item
            // =================================================

            itemDescriptionRegisterCode +=
                buildItemDescriptionRegisterCode(
                    fieldname,
                    fieldKey,
                    description
                );


            // =================================================
            // LanguageProvider
            // =================================================

            itemDescriptionCode +=
                buildDescriptionCode(
                    fieldKey,
                    description
                );


            // =================================================
            // ItemDescriptionConsts
            // =================================================

            itemDescriptionStaticCode +=
                buildDescriptionStaticCode(
                    fieldKey,
                    description
                );
        }
    }


    // =========================================================
    // Order Language Provider
    // =========================================================

    orderChatLangProviderCode +=
        orderChatCode;


    // =========================================================
    // 输出文件
    // =========================================================

    fs.writeFileSync(
        "./customer_data.javamethod",
        providerCode,
        "utf8"
    );

    fs.writeFileSync(
        "./customer_chat_lang.javamethod",
        chatCode,
        "utf8"
    );

    fs.writeFileSync(
        "./customer_description_lang.javamethod",
        descriptionCode,
        "utf8"
    );

    fs.writeFileSync(
        "./RoleChatConsts.javamethod",
        chatStaticCode,
        "utf8"
    );

    fs.writeFileSync(
        "./RoleDescriptionConsts.javamethod",
        descriptionStaticCode,
        "utf8"
    );

    fs.writeFileSync(
        "./CustomerOrderLang.javamethod",
        orderChatLangProviderCode,
        "utf8"
    );

    fs.writeFileSync(
        "./CustomerOrderConsts.javamethod",
        orderChatStaticCode,
        "utf8"
    );

    fs.writeFileSync(
        "./ItemDescriptionRegisterCode.javamethod",
        itemDescriptionRegisterCode,
        "utf8"
    );

    fs.writeFileSync(
        "./ItemDescriptionCode.javamethod",
        itemDescriptionCode,
        "utf8"
    );

    fs.writeFileSync(
        "./ItemDescriptionStaticCode.javamethod",
        itemDescriptionStaticCode,
        "utf8"
    );

    fs.writeFileSync(
        "./customer_evaluation_lang.javamethod",
        evaluationCode,
        "utf8"
    );

    fs.writeFileSync(
        "./CustomerEvaluationConsts.javamethod",
        evaluationStaticCode,
        "utf8"
    );
}


await main();