import os
import json

# ==============================================================================
# НАСТРОЙКИ МОДА
# ==============================================================================
MOD_ID = "create_additional_energy_soursesы"  # ЗАМЕНИТЕ НА ID СВОЕГО МОДА

# Базовые пути для рецептов Create
PATH_PRESSING = f"data/{MOD_ID}/recipes/pressing"
PATH_BASIN_PRESSING = f"data/{MOD_ID}/recipes/basin_pressing"
PATH_MIXING = f"data/{MOD_ID}/recipes/mixing"
PATH_MECH_CRAFT = f"data/{MOD_ID}/recipes/mechanical_crafting"
PATH_CRUSHING = f"data/{MOD_ID}/recipes/crushing"
PATH_SEQUENCED = f"data/{MOD_ID}/recipes/sequenced_assembly"

# Создаем структуру папок
for path in [PATH_PRESSING, PATH_BASIN_PRESSING, PATH_MIXING, PATH_MECH_CRAFT, PATH_CRUSHING, PATH_SEQUENCED]:
    os.makedirs(path, exist_ok=True)

# Списки ваших металлов и предметов
metals = ["iron", "aluminum", "titanium"]
placeholders = ["minecraft:iron_block", "minecraft:diamond_block", "minecraft:netherite_block"]

# ==============================================================================
# 1. ГЕНЕРАЦИЯ ДЛЯ МЕТАЛЛОВ (Поршни, Валы, Картеры, Двигатели, ГБЦ, ЭБУ)
# ==============================================================================
for i, metal in enumerate(metals):
    # ID ваших предметов из мода
    ingot = f"{MOD_ID}:{metal}_ingot"
    piston = f"{MOD_ID}:{metal}_piston"
    crankshaft = f"{MOD_ID}:{metal}_crankshaft"

    carter_i4 = f"{MOD_ID}:{metal}_i4_carter_item"
    carter_v8 = f"{MOD_ID}:{metal}_v8_carter_item"
    carter_w16 = f"{MOD_ID}:{metal}_w16_carter_item"
    carter_r32 = f"{MOD_ID}:{metal}_r32_carter_item"

    gbc = f"{MOD_ID}:{metal}_gbc"
    brain = f"{MOD_ID}:{metal}_brain_gbc"
    nugget = f"{MOD_ID}:{metal}_nugget" if metal != "iron" else "minecraft:iron_nugget"

    # --- ПОРШНИ (Прессование в чаше) ---
    with open(f"{PATH_BASIN_PRESSING}/piston_{metal}.json", "w", encoding="utf-8") as f:
        json.dump({
            "type": "create:pressing",
            "ingredients": [{"item": ingot}, {"item": "create:shaft"}, {"item": "create:zinc_nugget"}],
            "results": [{"item": piston}],
            "requires_basin": True
        }, f, indent=2)

    # --- КОЛЕНВАЛЫ (Обычное прессование) ---
    with open(f"{PATH_PRESSING}/crankshaft_{metal}.json", "w", encoding="utf-8") as f:
        json.dump({
            "type": "create:pressing",
            "ingredients": [{"item": ingot}],
            "results": [{"item": crankshaft}]
        }, f, indent=2)

    # --- КАРТЕР I4 (Прессование из 2 слитков) ---
    with open(f"{PATH_PRESSING}/carter_i4_{metal}.json", "w", encoding="utf-8") as f:
        json.dump({
            "type": "create:pressing",
            "ingredients": [{"item": ingot}, {"item": ingot}],
            "results": [{"item": carter_i4}]
        }, f, indent=2)

    # --- КАРТЕР V8 (Смешивание с лавой, х2 I4) ---
    with open(f"{PATH_MIXING}/carter_v8_{metal}.json", "w", encoding="utf-8") as f:
        json.dump({
            "type": "create:mixing",
            "ingredients": [{"item": carter_i4}, {"item": carter_i4}, {"fluid": "minecraft:lava", "amount": 1000}],
            "results": [{"item": carter_v8}],
            "heatRequirement": "superheated"
        }, f, indent=2)

    # --- КАРТЕР W16 (Смешивание с лавой, х4 I4) ---
    with open(f"{PATH_MIXING}/carter_w16_{metal}.json", "w", encoding="utf-8") as f:
        json.dump({
            "type": "create:mixing",
            "ingredients": [{"item": carter_i4}, {"item": carter_i4}, {"item": carter_i4}, {"item": carter_i4}, {"fluid": "minecraft:lava", "amount": 1000}],
            "results": [{"item": carter_w16}],
            "heatRequirement": "superheated"
        }, f, indent=2)

    # --- КАРТЕР R32 (Смешивание с лавой, х8 I4) ---
    with open(f"{PATH_MIXING}/carter_r32_{metal}.json", "w", encoding="utf-8") as f:
        json.dump({
            "type": "create:mixing",
            "ingredients": [{"item": carter_i4}] * 8 + [{"fluid": "minecraft:lava", "amount": 1000}],
            "results": [{"item": carter_r32}],
            "heatRequirement": "superheated"
        }, f, indent=2)

    # --- ГБЦ (Дробление / Вытачивание на шлифовальных колесах) ---
    with open(f"{PATH_CRUSHING}/gbc_{metal}.json", "w", encoding="utf-8") as f:
        json.dump({
            "type": "create:crushing",
            "ingredients": [{"item": ingot}],
            "results": [{"item": gbc}],
            "processingTime": 250
        }, f, indent=2)

    # --- ЭБУ / МОЗГИ (Последовательная сборка на конвейере) ---
    with open(f"{PATH_SEQUENCED}/brain_gbc_{metal}.json", "w", encoding="utf-8") as f:
        json.dump({
            "type": "create:sequenced_assembly",
            "ingredient": {"item": "create:electron_tube"},  # Базовая заготовка
            "transitionalItem": "create:incomplete_electron_tube", # Промежуточный предмет (ванильный)
            "sequence": [
                {
                    "type": "create:deploying",
                    "ingredients": [{"item": "create:incomplete_electron_tube"}, {"item": "minecraft:gold_nugget"}],
                    "results": [{"item": "create:incomplete_electron_tube"}]
                },
                {
                    "type": "create:deploying",
                    "ingredients": [{"item": "create:incomplete_electron_tube"}, {"item": "minecraft:redstone"}],
                    "results": [{"item": "create:incomplete_electron_tube"}]
                },
                {
                    "type": "create:deploying",
                    "ingredients": [{"item": "create:incomplete_electron_tube"}, {"item": nugget}],
                    "results": [{"item": "create:incomplete_electron_tube"}]
                }
            ],
            "results": [
                {"item": brain, "chance": 1.0}
            ],
            "loops": 1
        }, f, indent=2)

    # --- ДВИГАТЕЛИ-ЗАГЛУШКИ (Сжатие R32 + поршень) ---
    with open(f"{PATH_PRESSING}/engine_block_{metal}.json", "w", encoding="utf-8") as f:
        json.dump({
            "type": "create:pressing",
            "ingredients": [{"item": carter_r32}, {"item": piston}],
            "results": [{"item": placeholders[i]}],
            "requires_basin": True
        }, f, indent=2)


# ==============================================================================
# 2. ГЕНЕРАЦИЯ РАДИАТОРОВ (Механический сборщик 5х3)
# ==============================================================================
radiators = [
    {"name": "copper", "result": f"{MOD_ID}:radiator_copper", "ingot": "minecraft:copper_ingot", "plate": "create:copper_sheet"},
    {"name": "steel", "result": f"{MOD_ID}:radiator_steel", "ingot": "minecraft:iron_ingot", "plate": "create:iron_sheet"},
    {"name": "gold", "result": f"{MOD_ID}:radiator_gold", "ingot": "minecraft:gold_ingot", "plate": "create:golden_sheet"},
    {"name": "titanium", "result": f"{MOD_ID}:radiator_titanium", "ingot": f"{MOD_ID}:titanium_ingot", "plate": "create:iron_sheet"}
]

for rad in radiators:
    with open(f"{PATH_MECH_CRAFT}/radiator_{rad['name']}.json", "w", encoding="utf-8") as f:
        json.dump({
            "type": "create:mechanical_crafting",
            "pattern": ["PIIIP", "PSSSP", "PIIIP"],
            "key": {
                "P": {"item": "create:fluid_pipe"},
                "I": {"item": rad["ingot"]},
                "S": {"item": rad["plate"]}
            },
            "result": {"item": rad["result"]}
        }, f, indent=2)

print("Успех! Тонна JSON-файлов (включая ГБЦ и ЭБУ) успешно создана.")
