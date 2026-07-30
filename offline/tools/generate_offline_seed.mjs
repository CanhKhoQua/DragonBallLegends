import fs from "fs";

const [, , inputPath, outputPath] = process.argv;
if (!inputPath || !outputPath) {
  console.error("Usage: node offline/tools/generate_offline_seed.mjs <dump.sql> <offline_seed.sql>");
  process.exit(1);
}

const keepDataTables = new Set([
  "achievement_template",
  "array_head_2_frames",
  "bg_item_template",
  "caption",
  "clan_task_template",
  "data_badges",
  "event",
  "flag_bag",
  "head_avatar",
  "img_by_name",
  "intrinsic",
  "item_option_template",
  "item_template",
  "map_template",
  "mob_template",
  "moc_capsule_trang_suc",
  "moc_hopqua2010",
  "moc_nap",
  "moc_nap_top",
  "moc_san_boss",
  "moc_suc_manh",
  "moc_suc_manh_top",
  "moc_thiepchucvip",
  "notify",
  "npc_template",
  "options",
  "part",
  "power_limit",
  "radar",
  "shop",
  "side_task_template",
  "skill_template",
  "tab_shop",
  "task_badges_template",
  "task_kol_template",
  "task_main_template",
  "task_sub_template",
  "top_template",
  "type_item",
  "type_sell_item_shop",
]);

function insertTableName(line) {
  const match = line.match(/^INSERT\s+INTO\s+`?([^`\s(]+)`?/i);
  return match ? match[1] : null;
}

const lines = fs.readFileSync(inputPath, "utf8").split(/\r?\n/);
const out = [
  "-- DragonBallLegends Offline seed",
  "-- Generated from local dump with account/player/log/payment data removed.",
  'SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";',
  'SET time_zone = "+00:00";',
  "SET NAMES utf8mb4;",
  "",
];

let skippingInsert = false;
let keepingInsert = false;

for (const line of lines) {
  if (/^-- Host:|^-- Generation Time:|^-- Server version:|^-- PHP Version:/i.test(line)) {
    continue;
  }

  if (skippingInsert) {
    if (line.trimEnd().endsWith(";")) skippingInsert = false;
    continue;
  }

  if (keepingInsert) {
    out.push(line);
    if (line.trimEnd().endsWith(";")) keepingInsert = false;
    continue;
  }

  const table = insertTableName(line);
  if (table) {
    if (keepDataTables.has(table)) {
      out.push(line);
      if (!line.trimEnd().endsWith(";")) keepingInsert = true;
    } else if (!line.trimEnd().endsWith(";")) {
      skippingInsert = true;
    }
    continue;
  }

  if (/^LOCK\s+TABLES|^UNLOCK\s+TABLES/i.test(line)) continue;
  if (/^--\s+Dumping data for table/i.test(line)) continue;

  out.push(line);
}

out.push("");
out.push("-- Offline-safe public settings. Payment/bank/captcha secrets are scrubbed.");
out.push("INSERT INTO `settings` (`Title`, `Description`, `Keywords`, `SiteKey`, `SecretKey`, `ServerName`, `Fanpage`, `Group`, `Zalo`, `EmailSupport`, `AccountBank`, `PasswordBank`, `NumberBank`, `NameBank`, `Android`, `Windows`, `IPhone`, `Java`) VALUES");
out.push("('DragonBallLegends Offline', 'Offline local server', NULL, '', '', 'DragonBallLegends Offline', NULL, NULL, NULL, 'support@example.com', '', '', NULL, '', NULL, NULL, NULL, NULL);");
out.push("");
out.push("-- Demo local account. Login with offline / 123456, then create a character.");
out.push("INSERT INTO `account` (`id`, `username`, `password`, `email`, `active`, `admin`, `DiemDanh`, `vip`) VALUES");
out.push("(1, 'offline', '123456', '', 1, 0, 0, 4);");

fs.writeFileSync(outputPath, out.join("\n"), "utf8");
