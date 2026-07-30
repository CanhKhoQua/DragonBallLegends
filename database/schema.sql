-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `nroserver`
--

DELIMITER $$
--
-- Functions
--
CREATE DEFINER=`root`@`localhost` FUNCTION `SPLIT_STR` (`x` VARCHAR(255), `delim` VARCHAR(12), `pos` INT) RETURNS VARCHAR(255) CHARSET utf8mb4 COLLATE utf8mb4_general_ci DETERMINISTIC BEGIN
    RETURN TRIM(
        REPLACE(
            SUBSTRING(
                SUBSTRING_INDEX(x, delim, pos),
                LENGTH(SUBSTRING_INDEX(x, delim, pos - 1)) + 1
            ),
            delim, ''
        )
    );
END$$

DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `account`
--

CREATE TABLE `account` (
  `id` int(11) NOT NULL,
  `username` varchar(20) NOT NULL,
  `password` varchar(100) NOT NULL,
  `email` text DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT current_timestamp(),
  `update_time` timestamp NULL DEFAULT current_timestamp(),
  `ban` smallint(6) NOT NULL DEFAULT 0,
  `point_post` int(11) NOT NULL DEFAULT 0,
  `last_post` int(11) NOT NULL DEFAULT 0,
  `role` int(11) NOT NULL DEFAULT -1,
  `is_admin` tinyint(1) NOT NULL DEFAULT 0,
  `last_time_login` timestamp NOT NULL DEFAULT '2002-07-30 17:00:00',
  `last_time_logout` timestamp NOT NULL DEFAULT '2002-07-30 17:00:00',
  `ip_address` varchar(50) DEFAULT NULL,
  `active` int(11) NOT NULL DEFAULT 0,
  `thoi_vang` int(11) NOT NULL DEFAULT 0,
  `server_login` int(11) NOT NULL DEFAULT -1,
  `bd_player` double DEFAULT 1,
  `is_gift_box` tinyint(1) DEFAULT 0,
  `gift_time` varchar(255) DEFAULT '0',
  `reward` longtext DEFAULT NULL,
  `cash` int(11) NOT NULL DEFAULT 0,
  `vnd` int(11) NOT NULL DEFAULT 0,
  `tongnap` int(11) NOT NULL DEFAULT 0,
  `DiemDanh` int(11) NOT NULL,
  `lastDiemDanh` date DEFAULT NULL,
  `danap` int(11) NOT NULL DEFAULT 0,
  `diemboss` int(11) NOT NULL DEFAULT 0,
  `bong_master` int(11) NOT NULL DEFAULT 0,
  `hopquathang9vip` int(11) NOT NULL DEFAULT 0,
  `hopquathang9` int(11) NOT NULL DEFAULT 0,
  `hopquatrungthuvip` int(11) NOT NULL DEFAULT 0,
  `longdentreo` int(11) NOT NULL DEFAULT 0,
  `hoptrahoacuc` int(11) NOT NULL DEFAULT 0,
  `hopkeomaquy` int(11) NOT NULL DEFAULT 0,
  `capsuvip` int(11) NOT NULL DEFAULT 0,
  `thiepchucvip` int(11) NOT NULL DEFAULT 0,
  `hopqua2010` int(11) NOT NULL DEFAULT 0,
  `halloween_master` int(11) NOT NULL DEFAULT 0,
  `keo_halloween` int(11) NOT NULL DEFAULT 0,
  `thiep_halloween` int(11) NOT NULL DEFAULT 0,
  `diemnoel` int(11) NOT NULL DEFAULT 0,
  `hopdiem` int(11) NOT NULL DEFAULT 0,
  `vongquayvang` int(11) NOT NULL DEFAULT 0,
  `vongquaydacbiet` int(11) NOT NULL DEFAULT 0,
  `phaobong` int(11) NOT NULL DEFAULT 0,
  `lixi` int(11) NOT NULL DEFAULT 0,
  `xsrf_token` text DEFAULT NULL,
  `newpass` text DEFAULT NULL,
  `luotquay` int(11) NOT NULL DEFAULT 0,
  `vang` bigint(20) NOT NULL DEFAULT 0,
  `event_point` int(11) NOT NULL DEFAULT 0,
  `vip` int(11) NOT NULL DEFAULT 4,
  `mkc2` varchar(255) DEFAULT NULL,
  `admin` int(11) NOT NULL,
  `gioithieu` int(11) DEFAULT NULL,
  `gmail` varchar(100) DEFAULT NULL,
  `tichdiem` int(11) NOT NULL DEFAULT 0,
  `last_login_ip` varchar(45) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `achievement_template`
--

CREATE TABLE `achievement_template` (
  `id` int(11) NOT NULL,
  `info1` text NOT NULL,
  `info2` text NOT NULL,
  `money` int(11) NOT NULL,
  `max_count` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `adminpanel`
--

CREATE TABLE `adminpanel` (
  `domain` text DEFAULT NULL,
  `logo` text DEFAULT NULL,
  `trangthai` text NOT NULL,
  `android` text DEFAULT NULL,
  `android2` text DEFAULT NULL,
  `iphone` text DEFAULT NULL,
  `iphone2` text DEFAULT NULL,
  `windows` text DEFAULT NULL,
  `windows2` text DEFAULT NULL,
  `java` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `array_head_2_frames`
--

CREATE TABLE `array_head_2_frames` (
  `id` int(11) NOT NULL,
  `data` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `atm_check`
--

CREATE TABLE `atm_check` (
  `id` int(11) NOT NULL,
  `tranid` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

-- --------------------------------------------------------

--
-- Table structure for table `atm_lichsu`
--

CREATE TABLE `atm_lichsu` (
  `id` int(11) NOT NULL,
  `user_nap` varchar(20) DEFAULT NULL,
  `magiaodich` text NOT NULL,
  `thoigian` text DEFAULT NULL,
  `sotien` text DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  `accountNo` int(11) DEFAULT NULL,
  `benAccountName` varchar(255) DEFAULT NULL,
  `bankName` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

-- --------------------------------------------------------

--
-- Table structure for table `bank_history`
--

CREATE TABLE `bank_history` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `amount` int(11) NOT NULL,
  `transaction_id` varchar(100) NOT NULL,
  `note` text DEFAULT NULL,
  `status` enum('pending','success','failed') NOT NULL DEFAULT 'pending',
  `admin_note` text DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime DEFAULT NULL ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `bank_transfers`
--

CREATE TABLE `bank_transfers` (
  `id` int(11) NOT NULL,
  `transaction_id` varchar(255) NOT NULL,
  `username` varchar(255) NOT NULL,
  `amount` decimal(15,2) NOT NULL,
  `description` text DEFAULT NULL,
  `status` varchar(50) NOT NULL,
  `sender_bank_name` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `is_credited` tinyint(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

-- --------------------------------------------------------

--
-- Table structure for table `bg_item_template`
--

CREATE TABLE `bg_item_template` (
  `id` int(11) NOT NULL,
  `image_id` int(11) NOT NULL,
  `layer` int(11) NOT NULL,
  `dx` int(11) NOT NULL,
  `dy` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `boss_tower_weekly`
--

CREATE TABLE `boss_tower_weekly` (
  `id` int(11) NOT NULL,
  `player_id` int(11) NOT NULL,
  `week_key` varchar(10) NOT NULL,
  `max_floor` int(11) NOT NULL DEFAULT 0,
  `best_time` int(11) NOT NULL DEFAULT 0,
  `claimed_floor` int(11) NOT NULL DEFAULT 0,
  `claimed` tinyint(1) NOT NULL DEFAULT 0,
  `top_claimed` tinyint(1) NOT NULL DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `buff_vnd_history`
--

CREATE TABLE `buff_vnd_history` (
  `id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `amount_vnd` int(11) NOT NULL,
  `cash_added` int(11) NOT NULL,
  `payment_type` enum('card','bank') NOT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `admin_name` varchar(50) DEFAULT NULL,
  `created_at` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `caption`
--

CREATE TABLE `caption` (
  `id` int(11) NOT NULL,
  `earth` text NOT NULL,
  `saiya` text NOT NULL,
  `namek` text NOT NULL,
  `power` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `card_history`
--

CREATE TABLE `card_history` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `telco` varchar(20) NOT NULL COMMENT 'Nhà mạng: VIETTEL, VINAPHONE, MOBIFONE',
  `amount` int(11) NOT NULL COMMENT 'Mệnh giá thẻ',
  `real_amount` int(11) DEFAULT 0 COMMENT 'Mệnh giá thực tế (sau khi check)',
  `receive_amount` int(11) DEFAULT 0 COMMENT 'Số tiền nhận được (sau chiết khấu)',
  `code` varchar(50) NOT NULL COMMENT 'Mã thẻ',
  `serial` varchar(50) NOT NULL COMMENT 'Serial thẻ',
  `status` varchar(20) NOT NULL DEFAULT 'pending' COMMENT 'pending, success, failed',
  `trans_id` varchar(100) DEFAULT NULL COMMENT 'Mã giao dịch từ Thesieure',
  `message` text DEFAULT NULL COMMENT 'Thông báo kết quả',
  `created_at` datetime NOT NULL,
  `updated_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `clan`
--

CREATE TABLE `clan` (
  `id` int(11) NOT NULL,
  `NAME` varchar(255) NOT NULL,
  `NAME_2` varchar(4) NOT NULL,
  `slogan` varchar(255) NOT NULL DEFAULT '',
  `img_id` int(11) NOT NULL DEFAULT 0,
  `power_point` bigint(20) NOT NULL DEFAULT 0,
  `max_member` smallint(6) NOT NULL DEFAULT 10,
  `clan_point` int(11) NOT NULL DEFAULT 0,
  `LEVEL` int(11) NOT NULL DEFAULT 1,
  `members` text NOT NULL,
  `tops` text NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT current_timestamp(),
  `thanhTichBDKB` varchar(255) NOT NULL DEFAULT '[0,0]',
  `thongTinLeader` varchar(255) NOT NULL DEFAULT '[0,0,0,0,0]',
  `thanhTichKhiGas` varchar(255) NOT NULL DEFAULT '[0,0]',
  `thanhTichCDRD` varchar(255) NOT NULL DEFAULT '[0,0]',
  `thongTinLeader2` varchar(255) NOT NULL DEFAULT '[0,0,0,0,0]',
  `thongTinLeader3` varchar(255) NOT NULL DEFAULT '[0,0,0,0,0]'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `clan_task_template`
--

CREATE TABLE `clan_task_template` (
  `id` int(11) NOT NULL,
  `NAME` varchar(255) NOT NULL,
  `max_count_lv1` varchar(255) NOT NULL,
  `max_count_lv2` varchar(255) NOT NULL,
  `max_count_lv3` varchar(255) NOT NULL,
  `max_count_lv4` varchar(255) NOT NULL,
  `max_count_lv5` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `comments`
--

CREATE TABLE `comments` (
  `id` int(11) NOT NULL,
  `post_id` int(11) NOT NULL,
  `nguoidung` varchar(255) NOT NULL,
  `traloi` text NOT NULL,
  `gender` int(11) NOT NULL DEFAULT 0,
  `admin` int(11) NOT NULL DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `binhluan` int(11) NOT NULL DEFAULT 0,
  `image` varchar(255) DEFAULT NULL,
  `server` int(11) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

-- --------------------------------------------------------

--
-- Table structure for table `cpanel`
--

CREATE TABLE `cpanel` (
  `userlogin` varchar(20) DEFAULT NULL,
  `stk` varchar(20) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `sessionId` varchar(255) DEFAULT NULL,
  `deviceId` varchar(255) DEFAULT NULL,
  `token` varchar(255) DEFAULT NULL,
  `time` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `data_badges`
--

CREATE TABLE `data_badges` (
  `id` int(11) NOT NULL,
  `idEffect` int(11) NOT NULL,
  `idItem` int(11) NOT NULL,
  `NAME` text CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `Options` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `event`
--

CREATE TABLE `event` (
  `id` int(11) NOT NULL,
  `name` text NOT NULL,
  `data` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `exchange_rates`
--

CREATE TABLE `exchange_rates` (
  `id` int(11) NOT NULL,
  `card_rate` decimal(10,4) NOT NULL DEFAULT 1.0000,
  `bank_rate` decimal(10,4) NOT NULL DEFAULT 1.0000,
  `min_amount` int(11) NOT NULL DEFAULT 10000,
  `max_amount` int(11) NOT NULL DEFAULT 1000000,
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `flag_bag`
--

CREATE TABLE `flag_bag` (
  `id` int(11) NOT NULL,
  `icon_data` varchar(1000) NOT NULL,
  `NAME` varchar(255) NOT NULL DEFAULT 'flag_bag',
  `gold` int(11) NOT NULL DEFAULT -1,
  `gem` int(11) NOT NULL DEFAULT -1,
  `icon_id` int(11) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `giftcode`
--

CREATE TABLE `giftcode` (
  `id` int(11) NOT NULL,
  `code` text NOT NULL,
  `count_left` int(11) NOT NULL,
  `detail` text NOT NULL,
  `datecreate` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `expired` timestamp NOT NULL DEFAULT '2025-10-09 17:00:00',
  `type` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `head_avatar`
--

CREATE TABLE `head_avatar` (
  `head_id` int(11) NOT NULL,
  `avatar_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `history_bank`
--

CREATE TABLE `history_bank` (
  `id` int(11) NOT NULL,
  `username` varchar(255) NOT NULL,
  `code` varchar(100) NOT NULL,
  `amount_vnd` decimal(12,2) NOT NULL,
  `amount_ruby` decimal(12,2) NOT NULL,
  `description` varchar(500) NOT NULL,
  `status` varchar(50) DEFAULT 'pending',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `history_card`
--

CREATE TABLE `history_card` (
  `id` int(11) NOT NULL,
  `username` varchar(255) NOT NULL,
  `telco` varchar(50) NOT NULL,
  `amount` decimal(12,2) NOT NULL,
  `amount_received` decimal(12,2) NOT NULL,
  `serial` varchar(255) NOT NULL,
  `code` varchar(255) NOT NULL,
  `request_id` varchar(100) NOT NULL,
  `status` varchar(50) DEFAULT 'pending',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `history_items_diemdanh`
--

CREATE TABLE `history_items_diemdanh` (
  `id` int(11) NOT NULL,
  `account_id` int(11) NOT NULL,
  `item_temp_id` int(11) NOT NULL,
  `bought_date` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `history_transaction`
--

CREATE TABLE `history_transaction` (
  `id` int(11) NOT NULL,
  `player_1` varchar(255) NOT NULL,
  `player_2` varchar(255) NOT NULL,
  `item_player_1` text NOT NULL,
  `item_player_2` text NOT NULL,
  `bag_1_before_tran` text NOT NULL,
  `bag_2_before_tran` text NOT NULL,
  `bag_1_after_tran` text NOT NULL,
  `bag_2_after_tran` text NOT NULL,
  `time_tran` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `hist_quydoitv`
--

CREATE TABLE `hist_quydoitv` (
  `id` int(10) UNSIGNED NOT NULL,
  `account_id` int(10) UNSIGNED NOT NULL COMMENT 'ID tài khoản',
  `cash_used` bigint(20) UNSIGNED NOT NULL COMMENT 'Số cash/VND đã dùng',
  `gold_received` bigint(20) UNSIGNED NOT NULL COMMENT 'Số thỏi vàng nhận',
  `event_item` int(10) UNSIGNED NOT NULL COMMENT 'ID item sự kiện',
  `event_quantity` int(10) UNSIGNED NOT NULL COMMENT 'Số lượng item sự kiện',
  `time_create` datetime NOT NULL DEFAULT current_timestamp() COMMENT 'Thời gian quy đổi'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `img_by_name`
--

CREATE TABLE `img_by_name` (
  `id` int(11) NOT NULL,
  `NAME` varchar(55) NOT NULL,
  `n_frame` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `intrinsic`
--

CREATE TABLE `intrinsic` (
  `id` int(11) NOT NULL,
  `NAME` varchar(255) NOT NULL,
  `param_from_1` int(11) NOT NULL DEFAULT 0,
  `param_to_1` int(11) NOT NULL DEFAULT 0,
  `param_from_2` int(11) NOT NULL DEFAULT 0,
  `param_to_2` int(11) NOT NULL DEFAULT 0,
  `icon` int(11) NOT NULL DEFAULT 0,
  `gender` smallint(6) NOT NULL DEFAULT 3
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `items_web`
--

CREATE TABLE `items_web` (
  `id` int(11) NOT NULL,
  `vnd` int(11) NOT NULL,
  `items` int(11) NOT NULL,
  `slot` int(11) NOT NULL,
  `options` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `item_option_template`
--

CREATE TABLE `item_option_template` (
  `id` int(11) NOT NULL,
  `NAME` varchar(255) NOT NULL,
  `type` smallint(6) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `item_template`
--

CREATE TABLE `item_template` (
  `id` int(11) NOT NULL,
  `TYPE` int(11) NOT NULL,
  `gender` smallint(6) NOT NULL,
  `NAME` varchar(255) NOT NULL,
  `description` varchar(255) NOT NULL,
  `level` int(11) NOT NULL DEFAULT 0,
  `icon_id` int(11) NOT NULL,
  `part` int(11) NOT NULL,
  `is_up_to_up` tinyint(1) NOT NULL,
  `power_require` bigint(20) NOT NULL,
  `gold` int(11) NOT NULL DEFAULT 0,
  `gem` int(11) NOT NULL DEFAULT 0,
  `head` int(11) NOT NULL DEFAULT -1,
  `body` int(11) NOT NULL DEFAULT -1,
  `leg` int(11) NOT NULL DEFAULT -1,
  `is_up_to_up_over_99` tinyint(1) NOT NULL DEFAULT 0,
  `can_trade` tinyint(1) NOT NULL DEFAULT 1,
  `comment` varchar(2000) DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `map_template`
--

CREATE TABLE `map_template` (
  `id` int(11) NOT NULL,
  `NAME` varchar(55) NOT NULL,
  `zones` int(11) NOT NULL DEFAULT 1,
  `max_player` int(11) NOT NULL DEFAULT 15,
  `data` varchar(1000) NOT NULL DEFAULT '[]',
  `type` int(11) NOT NULL DEFAULT 1,
  `planet_id` int(11) NOT NULL DEFAULT 1,
  `bg_type` int(11) NOT NULL DEFAULT 1,
  `tile_id` int(11) NOT NULL DEFAULT 1,
  `bg_id` int(11) NOT NULL DEFAULT 1,
  `waypoints` text NOT NULL,
  `mobs` text NOT NULL,
  `npcs` text NOT NULL,
  `is_map_double` int(11) NOT NULL DEFAULT 0,
  `effect` text NOT NULL,
  `eff_event` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `mbbank`
--

CREATE TABLE `mbbank` (
  `id` int(11) NOT NULL,
  `tranId` varchar(255) NOT NULL,
  `io` varchar(255) NOT NULL,
  `amount` int(11) NOT NULL,
  `comment` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

-- --------------------------------------------------------

--
-- Table structure for table `mob_template`
--

CREATE TABLE `mob_template` (
  `id` int(11) NOT NULL,
  `TYPE` int(11) NOT NULL,
  `NAME` varchar(50) NOT NULL,
  `hp` int(11) NOT NULL,
  `range_move` smallint(6) NOT NULL,
  `speed` smallint(6) NOT NULL,
  `dart_type` smallint(6) NOT NULL,
  `percent_dame` smallint(6) NOT NULL DEFAULT 5,
  `percent_tiem_nang` smallint(6) NOT NULL DEFAULT 50
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `moc_capsule_trang_suc`
--

CREATE TABLE `moc_capsule_trang_suc` (
  `id` int(11) NOT NULL,
  `info` text NOT NULL,
  `detail` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `moc_hopqua2010`
--

CREATE TABLE `moc_hopqua2010` (
  `id` int(11) NOT NULL,
  `info` text NOT NULL,
  `detail` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `moc_nap`
--

CREATE TABLE `moc_nap` (
  `id` int(11) NOT NULL,
  `info` text NOT NULL,
  `detail` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `moc_nap_top`
--

CREATE TABLE `moc_nap_top` (
  `id` int(11) NOT NULL,
  `info` text NOT NULL,
  `detail` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `moc_san_boss`
--

CREATE TABLE `moc_san_boss` (
  `id` int(11) NOT NULL,
  `info` text NOT NULL,
  `detail` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `moc_suc_manh`
--

CREATE TABLE `moc_suc_manh` (
  `id` int(11) NOT NULL,
  `info` text NOT NULL,
  `detail` text NOT NULL,
  `power` varchar(20) NOT NULL DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `moc_suc_manh_top`
--

CREATE TABLE `moc_suc_manh_top` (
  `id` int(11) NOT NULL,
  `info` text NOT NULL,
  `detail` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `moc_thiepchucvip`
--

CREATE TABLE `moc_thiepchucvip` (
  `id` int(11) NOT NULL,
  `info` text NOT NULL,
  `detail` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `napthe`
--

CREATE TABLE `napthe` (
  `id` int(11) NOT NULL,
  `user_nap` varchar(100) NOT NULL,
  `telco` varchar(255) NOT NULL,
  `serial` varchar(255) NOT NULL,
  `code` varchar(255) NOT NULL,
  `amount` int(11) NOT NULL,
  `status` int(11) NOT NULL,
  `request_id` varchar(255) NOT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `card_telco` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

-- --------------------------------------------------------

--
-- Table structure for table `naptien`
--

CREATE TABLE `naptien` (
  `id` int(11) NOT NULL,
  `uid` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sotien` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `seri` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `code` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `loaithe` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `time` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `noidung` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `tinhtrang` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `tranid` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `magioithieu` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

-- --------------------------------------------------------

--
-- Table structure for table `notify`
--

CREATE TABLE `notify` (
  `id` int(11) NOT NULL,
  `name` text NOT NULL,
  `text` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `npc_template`
--

CREATE TABLE `npc_template` (
  `id` int(11) NOT NULL,
  `NAME` varchar(50) NOT NULL,
  `head` int(11) NOT NULL,
  `body` int(11) NOT NULL,
  `leg` int(11) NOT NULL,
  `avatar` int(11) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `options`
--

CREATE TABLE `options` (
  `Id` bigint(20) DEFAULT NULL,
  `Name` varchar(1024) DEFAULT NULL,
  `Type` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `part`
--

CREATE TABLE `part` (
  `id` int(11) NOT NULL,
  `TYPE` int(11) NOT NULL,
  `DATA` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `payments`
--

CREATE TABLE `payments` (
  `id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `refNo` varchar(255) NOT NULL,
  `date` datetime NOT NULL,
  `card_serial` varchar(255) DEFAULT NULL,
  `card_pin` varchar(255) DEFAULT NULL,
  `declared_amount` int(11) NOT NULL,
  `api_declared_value` int(11) DEFAULT NULL,
  `detected_value` int(11) DEFAULT NULL,
  `received_amount_from_api` int(11) DEFAULT NULL,
  `final_credited_amount` int(11) DEFAULT 0,
  `status_text` varchar(255) NOT NULL,
  `api_status_code` varchar(50) NOT NULL,
  `api_message` text DEFAULT NULL,
  `card_telco` varchar(50) DEFAULT NULL,
  `is_credited` tinyint(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

-- --------------------------------------------------------

--
-- Table structure for table `player`
--

CREATE TABLE `player` (
  `id` int(11) NOT NULL,
  `account_id` int(11) DEFAULT NULL,
  `name` varchar(20) NOT NULL,
  `power` bigint(20) NOT NULL DEFAULT 0,
  `info` text NOT NULL,
  `head` int(11) NOT NULL DEFAULT 102,
  `gender` int(11) NOT NULL,
  `have_tennis_space_ship` tinyint(1) DEFAULT 0,
  `clan_id` int(11) NOT NULL DEFAULT -1,
  `data_inventory` text NOT NULL,
  `data_location` text NOT NULL,
  `data_point` text NOT NULL,
  `data_magic_tree` text NOT NULL,
  `items_body` text NOT NULL,
  `items_bag` text NOT NULL,
  `items_box` text NOT NULL,
  `items_box_collection` text NOT NULL,
  `items_box_lucky_round` text NOT NULL,
  `item_mails_box` text NOT NULL,
  `HocSkill` text DEFAULT NULL,
  `CheckHocSkill` text DEFAULT NULL,
  `items_daban` text NOT NULL,
  `friends` text NOT NULL,
  `enemies` text NOT NULL,
  `data_intrinsic` text NOT NULL,
  `data_item_time` text NOT NULL,
  `data_task` text NOT NULL,
  `data_mabu_egg` text NOT NULL,
  `data_charm` text NOT NULL,
  `skills` text NOT NULL,
  `skills_shortcut` text NOT NULL,
  `pet` text NOT NULL,
  `data_black_ball` text NOT NULL,
  `loadtimetop` text NOT NULL,
  `data_side_task` text NOT NULL,
  `data_kol_task` text NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT current_timestamp(),
  `firstTimeLogin` timestamp NOT NULL DEFAULT current_timestamp(),
  `notify` text CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL,
  `baovetaikhoan` varchar(1000) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL DEFAULT '[]',
  `captcha` varchar(1000) NOT NULL DEFAULT '[]',
  `data_card` varchar(10000) NOT NULL DEFAULT '[]',
  `lasttimepkcommeson` bigint(20) NOT NULL DEFAULT 0,
  `bandokhobau` varchar(250) NOT NULL DEFAULT '[]',
  `doanhtrai` bigint(20) NOT NULL DEFAULT 0,
  `conduongrandoc` varchar(255) NOT NULL DEFAULT '[]',
  `masterDoesNotAttack` text DEFAULT NULL,
  `nhanthoivang` varchar(200) NOT NULL DEFAULT '[]',
  `ruonggo` varchar(255) NOT NULL DEFAULT '[]',
  `sieuthanthuy` varchar(255) NOT NULL DEFAULT '[]',
  `vodaisinhtu` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '[]',
  `rongxuong` bigint(20) NOT NULL DEFAULT 0,
  `data_item_event` varchar(255) NOT NULL DEFAULT '[]',
  `data_luyentap` text DEFAULT NULL,
  `data_clan_task` varchar(255) NOT NULL DEFAULT '[]',
  `data_vip` text DEFAULT NULL,
  `rank` int(11) NOT NULL DEFAULT 0,
  `data_super_rank` text DEFAULT NULL,
  `data_achievement` text DEFAULT NULL,
  `giftcode` text DEFAULT NULL,
  `dataBadges` text NOT NULL,
  `dataTaskBadges` text NOT NULL,
  `dailyGift` text NOT NULL,
  `lastTimeUpdateTask` bigint(20) NOT NULL DEFAULT 1,
  `event_point` int(11) NOT NULL DEFAULT 0,
  `event_point_boss` int(11) NOT NULL DEFAULT 0,
  `event_point_nhs` int(11) NOT NULL DEFAULT 0,
  `event_point_quai` int(11) NOT NULL DEFAULT 0,
  `diem_quy_lao` int(11) NOT NULL DEFAULT 0,
  `hp_point_fusion` int(11) NOT NULL DEFAULT 0,
  `mp_point_fusion` int(11) NOT NULL DEFAULT 0,
  `dame_point_fusion` int(11) NOT NULL DEFAULT 0,
  `Achievement` varchar(255) DEFAULT NULL,
  `Achievement_SucManh` varchar(255) DEFAULT NULL,
  `Achievement_DiemBoss` varchar(255) DEFAULT NULL,
  `phaobong` int(11) NOT NULL DEFAULT 0,
  `BoughtSkill` text DEFAULT NULL,
  `LearnSkill` text DEFAULT NULL,
  `thanhTichBang` varchar(255) NOT NULL DEFAULT '[0,0,0,0]',
  `thanhTichBang2` varchar(255) NOT NULL DEFAULT '[0,0,0,0]',
  `thanhTichBang3` varchar(255) NOT NULL DEFAULT '[0,0,0,0]',
  `thachdauwhis` int(11) NOT NULL DEFAULT 0,
  `point_maydam` int(11) NOT NULL DEFAULT 0,
  `total_damage_maydam` bigint(20) NOT NULL,
  `nhiem_vu_kol` text NOT NULL,
  `checkNhanQua` varchar(255) NOT NULL DEFAULT '[1,1,"1970-01-01T00:00:00"]',
  `costume_collection` text DEFAULT NULL,
  `items_costume_box` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `posts`
--

CREATE TABLE `posts` (
  `id` int(11) NOT NULL,
  `tieude` varchar(75) NOT NULL,
  `noidung` text NOT NULL,
  `username` varchar(50) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `theloai` int(11) NOT NULL DEFAULT 0,
  `ghimbai` int(11) NOT NULL DEFAULT 0,
  `image` varchar(255) DEFAULT NULL,
  `trangthai` int(11) NOT NULL DEFAULT 0,
  `tinhtrang` int(11) NOT NULL DEFAULT 0,
  `like` int(11) NOT NULL DEFAULT 0,
  `views` int(11) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `power_limit`
--

CREATE TABLE `power_limit` (
  `id` int(11) NOT NULL,
  `power` bigint(20) NOT NULL,
  `hp` int(11) NOT NULL,
  `mp` int(11) NOT NULL,
  `damage` int(11) NOT NULL,
  `defense` int(11) NOT NULL,
  `critical` int(11) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=FIXED;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `radar`
--

CREATE TABLE `radar` (
  `id` int(11) NOT NULL,
  `iconId` int(11) DEFAULT 0,
  `rank` tinyint(4) DEFAULT 0,
  `max` int(11) DEFAULT 60,
  `type` int(11) DEFAULT 0,
  `mob_id` int(11) DEFAULT 1,
  `body` varchar(500) DEFAULT '[]',
  `name` varchar(500) DEFAULT '',
  `info` varchar(2000) DEFAULT '',
  `options` varchar(2000) DEFAULT '[]',
  `aura_id` smallint(6) DEFAULT -1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `recharge_log`
--

CREATE TABLE `recharge_log` (
  `id` int(11) NOT NULL,
  `trans_id` varchar(100) DEFAULT NULL,
  `account_id` int(11) DEFAULT NULL,
  `amount` int(11) DEFAULT 0,
  `description` text DEFAULT NULL,
  `status` int(1) DEFAULT 0,
  `created_at` timestamp NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `settings`
--

CREATE TABLE `settings` (
  `Title` varchar(100) DEFAULT 'Nguyen Duc Kien',
  `Description` longtext DEFAULT NULL,
  `Keywords` longtext DEFAULT NULL,
  `SiteKey` varchar(100) DEFAULT NULL,
  `SecretKey` varchar(100) DEFAULT NULL,
  `ServerName` varchar(100) DEFAULT NULL,
  `Fanpage` varchar(100) DEFAULT NULL,
  `Group` varchar(100) DEFAULT NULL,
  `Zalo` varchar(100) DEFAULT NULL,
  `EmailSupport` varchar(50) DEFAULT NULL,
  `AccountBank` varchar(50) DEFAULT NULL,
  `PasswordBank` varchar(50) DEFAULT NULL,
  `NumberBank` int(11) DEFAULT NULL,
  `NameBank` varchar(50) DEFAULT NULL,
  `Android` varchar(50) DEFAULT NULL,
  `Windows` varchar(50) DEFAULT NULL,
  `IPhone` varchar(50) DEFAULT NULL,
  `Java` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `shop`
--

CREATE TABLE `shop` (
  `id` int(11) NOT NULL,
  `npc_id` int(11) NOT NULL,
  `tag_name` varchar(50) DEFAULT NULL,
  `type_shop` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `shop_ky_gui`
--

CREATE TABLE `shop_ky_gui` (
  `id` int(11) NOT NULL,
  `player_id` int(11) NOT NULL,
  `tab` int(11) NOT NULL,
  `item_id` int(11) NOT NULL,
  `gold` int(11) NOT NULL,
  `gem` int(11) NOT NULL,
  `quantity` int(11) NOT NULL,
  `itemOption` text NOT NULL,
  `isUpTop` int(11) NOT NULL,
  `isBuy` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci ROW_FORMAT=DYNAMIC;

-- --------------------------------------------------------

--
-- Table structure for table `side_task_template`
--

CREATE TABLE `side_task_template` (
  `id` int(11) NOT NULL,
  `NAME` varchar(255) NOT NULL,
  `max_count_lv1` varchar(255) NOT NULL,
  `max_count_lv2` varchar(255) NOT NULL,
  `max_count_lv3` varchar(255) NOT NULL,
  `max_count_lv4` varchar(255) NOT NULL,
  `max_count_lv5` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `skill_template`
--

CREATE TABLE `skill_template` (
  `nclass_id` int(11) NOT NULL,
  `id` int(11) NOT NULL,
  `NAME` varchar(50) NOT NULL,
  `max_point` smallint(6) NOT NULL DEFAULT 7,
  `mana_use_type` smallint(6) NOT NULL,
  `TYPE` smallint(6) NOT NULL,
  `icon_id` int(11) NOT NULL,
  `dam_info` varchar(255) NOT NULL,
  `slot` int(11) NOT NULL,
  `skills` text NOT NULL,
  `description` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `tab_shop`
--

CREATE TABLE `tab_shop` (
  `id` int(11) NOT NULL,
  `shop_id` int(11) NOT NULL,
  `tab_name` varchar(50) NOT NULL,
  `tab_index` int(11) NOT NULL DEFAULT 0,
  `items` text NOT NULL,
  `Mô tả` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `task_badges_template`
--

CREATE TABLE `task_badges_template` (
  `id` int(11) NOT NULL,
  `NAME` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `maxCount` int(11) NOT NULL DEFAULT 0,
  `idBadgesReward` int(11) NOT NULL DEFAULT -1
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `task_kol_template`
--

CREATE TABLE `task_kol_template` (
  `id` int(11) NOT NULL,
  `info` varchar(255) NOT NULL,
  `max_count` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `task_main_template`
--

CREATE TABLE `task_main_template` (
  `id` int(11) NOT NULL,
  `NAME` varchar(255) NOT NULL,
  `detail` varchar(500) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `task_sub_template`
--

CREATE TABLE `task_sub_template` (
  `task_main_id` int(11) NOT NULL,
  `NAME` varchar(255) NOT NULL,
  `max_count` int(11) NOT NULL DEFAULT -1,
  `notify` varchar(255) NOT NULL DEFAULT '',
  `npc_id` int(11) NOT NULL DEFAULT -1,
  `map` int(11) NOT NULL,
  `id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `top_template`
--

CREATE TABLE `top_template` (
  `id` int(11) NOT NULL,
  `name` varchar(25) NOT NULL,
  `query` text NOT NULL,
  `date` datetime NOT NULL,
  `items` text NOT NULL,
  `users` text NOT NULL,
  `isDone` varchar(5) NOT NULL DEFAULT '0',
  `isAuto` varchar(5) NOT NULL DEFAULT '0',
  `limit` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `transaction_banking`
--

CREATE TABLE `transaction_banking` (
  `id` bigint(20) NOT NULL,
  `player_id` bigint(20) NOT NULL,
  `amount` bigint(20) NOT NULL,
  `description` varchar(6) NOT NULL,
  `status` bit(1) DEFAULT b'0',
  `is_recieve` bit(1) DEFAULT b'0',
  `last_time_check` timestamp NOT NULL DEFAULT current_timestamp(),
  `created_date` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `trans_log`
--

CREATE TABLE `trans_log` (
  `id` int(11) NOT NULL,
  `name` text NOT NULL,
  `amount` bigint(20) NOT NULL,
  `seri` text NOT NULL,
  `pin` text NOT NULL,
  `type` text NOT NULL,
  `status` int(11) NOT NULL DEFAULT 0,
  `trans_id` text NOT NULL,
  `date` timestamp NOT NULL DEFAULT current_timestamp(),
  `giatri` int(11) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

-- --------------------------------------------------------

--
-- Table structure for table `type_item`
--

CREATE TABLE `type_item` (
  `id` int(11) NOT NULL,
  `NAME` varchar(50) NOT NULL DEFAULT '',
  `index_body` int(11) NOT NULL DEFAULT -1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `type_sell_item_shop`
--

CREATE TABLE `type_sell_item_shop` (
  `id` int(11) NOT NULL,
  `NAME` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

--
--


-- --------------------------------------------------------

--
-- Table structure for table `zalo_groups`
--

CREATE TABLE `zalo_groups` (
  `id` int(11) NOT NULL,
  `group_id` varchar(50) NOT NULL,
  `name` text DEFAULT NULL,
  `member_total` int(11) DEFAULT 0,
  `boss_notify` tinyint(1) DEFAULT 0,
  `antispam` tinyint(1) DEFAULT 0,
  `created_at` datetime DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `zalo_users`
--

CREATE TABLE `zalo_users` (
  `id` int(11) NOT NULL,
  `uid` varchar(50) NOT NULL,
  `name` text DEFAULT NULL,
  `role` int(11) DEFAULT 0,
  `vnd` int(11) DEFAULT 0,
  `created_at` datetime DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `account`
--
ALTER TABLE `account`
  ADD PRIMARY KEY (`id`) USING BTREE,
  ADD UNIQUE KEY `username` (`username`) USING BTREE;

--
-- Indexes for table `achievement_template`
--
ALTER TABLE `achievement_template`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Indexes for table `array_head_2_frames`
--
ALTER TABLE `array_head_2_frames`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Indexes for table `atm_check`
--
ALTER TABLE `atm_check`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Indexes for table `atm_lichsu`
--
ALTER TABLE `atm_lichsu`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Indexes for table `bank_transfers`
--
ALTER TABLE `bank_transfers`
  ADD PRIMARY KEY (`id`) USING BTREE,
  ADD UNIQUE KEY `transaction_id` (`transaction_id`) USING BTREE,
  ADD KEY `username` (`username`) USING BTREE,
  ADD KEY `status` (`status`) USING BTREE;

--
-- Indexes for table `bg_item_template`
--
ALTER TABLE `bg_item_template`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Indexes for table `boss_tower_weekly`
--
ALTER TABLE `boss_tower_weekly`
  ADD PRIMARY KEY (`id`) USING BTREE,
  ADD UNIQUE KEY `uk_boss_tower_weekly_player_week` (`player_id`,`week_key`) USING BTREE,
  ADD KEY `idx_boss_tower_weekly_top` (`week_key`,`max_floor`,`best_time`) USING BTREE;

--
-- Indexes for table `caption`
--
ALTER TABLE `caption`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Indexes for table `clan`
--
ALTER TABLE `clan`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Indexes for table `clan_task_template`
--
ALTER TABLE `clan_task_template`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Indexes for table `comments`
--
ALTER TABLE `comments`
  ADD PRIMARY KEY (`id`) USING BTREE,
  ADD KEY `post_id` (`post_id`) USING BTREE;

--
-- Indexes for table `data_badges`
--
ALTER TABLE `data_badges`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Indexes for table `event`
--
ALTER TABLE `event`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Indexes for table `flag_bag`
--
ALTER TABLE `flag_bag`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Indexes for table `giftcode`
--
ALTER TABLE `giftcode`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Indexes for table `head_avatar`
--
ALTER TABLE `head_avatar`
  ADD PRIMARY KEY (`head_id`) USING BTREE;

--
-- Indexes for table `history_items_diemdanh`
--
ALTER TABLE `history_items_diemdanh`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `history_transaction`
--
ALTER TABLE `history_transaction`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Indexes for table `hist_quydoitv`
--
ALTER TABLE `hist_quydoitv`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_account_id` (`account_id`),
  ADD KEY `idx_time_create` (`time_create`);

--
-- Indexes for table `img_by_name`
--
ALTER TABLE `img_by_name`
  ADD PRIMARY KEY (`id`) USING BTREE,
  ADD UNIQUE KEY `NAME` (`NAME`) USING BTREE;

--
-- Indexes for table `intrinsic`
--
ALTER TABLE `intrinsic`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Indexes for table `item_option_template`
--
ALTER TABLE `item_option_template`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Indexes for table `item_template`
--
ALTER TABLE `item_template`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Indexes for table `map_template`
--
ALTER TABLE `map_template`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Indexes for table `mbbank`
--
ALTER TABLE `mbbank`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Indexes for table `mob_template`
--
ALTER TABLE `mob_template`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Indexes for table `moc_capsule_trang_suc`
--
ALTER TABLE `moc_capsule_trang_suc`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Indexes for table `moc_hopqua2010`
--
ALTER TABLE `moc_hopqua2010`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Indexes for table `moc_nap`
--
ALTER TABLE `moc_nap`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Indexes for table `moc_nap_top`
--
ALTER TABLE `moc_nap_top`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Indexes for table `moc_san_boss`
--
ALTER TABLE `moc_san_boss`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Indexes for table `moc_suc_manh`
--
ALTER TABLE `moc_suc_manh`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Indexes for table `moc_suc_manh_top`
--
ALTER TABLE `moc_suc_manh_top`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Indexes for table `moc_thiepchucvip`
--
ALTER TABLE `moc_thiepchucvip`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Indexes for table `napthe`
--
ALTER TABLE `napthe`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Indexes for table `naptien`
--
ALTER TABLE `naptien`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Indexes for table `notify`
--
ALTER TABLE `notify`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Indexes for table `npc_template`
--
ALTER TABLE `npc_template`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Indexes for table `part`
--
ALTER TABLE `part`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Indexes for table `payments`
--
ALTER TABLE `payments`
  ADD PRIMARY KEY (`id`) USING BTREE,
  ADD UNIQUE KEY `refNo` (`refNo`) USING BTREE;

--
-- Indexes for table `player`
--
ALTER TABLE `player`
  ADD PRIMARY KEY (`id`) USING BTREE,
  ADD UNIQUE KEY `account_id` (`account_id`) USING BTREE;

--
-- Indexes for table `posts`
--
ALTER TABLE `posts`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Indexes for table `power_limit`
--
ALTER TABLE `power_limit`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Indexes for table `recharge_log`
--
ALTER TABLE `recharge_log`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `shop`
--
ALTER TABLE `shop`
  ADD PRIMARY KEY (`id`) USING BTREE,
  ADD KEY `npc_id` (`npc_id`) USING BTREE;

--
-- Indexes for table `shop_ky_gui`
--
ALTER TABLE `shop_ky_gui`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Indexes for table `side_task_template`
--
ALTER TABLE `side_task_template`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Indexes for table `skill_template`
--
ALTER TABLE `skill_template`
  ADD PRIMARY KEY (`nclass_id`,`id`) USING BTREE;

--
-- Indexes for table `tab_shop`
--
ALTER TABLE `tab_shop`
  ADD PRIMARY KEY (`id`) USING BTREE,
  ADD KEY `shop_id` (`shop_id`) USING BTREE;

--
-- Indexes for table `task_badges_template`
--
ALTER TABLE `task_badges_template`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Indexes for table `task_kol_template`
--
ALTER TABLE `task_kol_template`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Indexes for table `task_main_template`
--
ALTER TABLE `task_main_template`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Indexes for table `task_sub_template`
--
ALTER TABLE `task_sub_template`
  ADD PRIMARY KEY (`id`) USING BTREE,
  ADD KEY `task_main_id` (`task_main_id`) USING BTREE;

--
-- Indexes for table `transaction_banking`
--
ALTER TABLE `transaction_banking`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `trans_log`
--
ALTER TABLE `trans_log`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Indexes for table `type_item`
--
ALTER TABLE `type_item`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Indexes for table `type_sell_item_shop`
--
ALTER TABLE `type_sell_item_shop`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- Indexes for table `zalo_groups`
--
ALTER TABLE `zalo_groups`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `group_id` (`group_id`);

--
-- Indexes for table `zalo_users`
--
ALTER TABLE `zalo_users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uid` (`uid`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `account`
--
ALTER TABLE `account`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=18;

--
-- AUTO_INCREMENT for table `achievement_template`
--
ALTER TABLE `achievement_template`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=21;

--
-- AUTO_INCREMENT for table `array_head_2_frames`
--
ALTER TABLE `array_head_2_frames`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=91;

--
-- AUTO_INCREMENT for table `atm_check`
--
ALTER TABLE `atm_check`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=539;

--
-- AUTO_INCREMENT for table `atm_lichsu`
--
ALTER TABLE `atm_lichsu`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=539;

--
-- AUTO_INCREMENT for table `bank_transfers`
--
ALTER TABLE `bank_transfers`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `boss_tower_weekly`
--
ALTER TABLE `boss_tower_weekly`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=54;

--
-- AUTO_INCREMENT for table `caption`
--
ALTER TABLE `caption`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=22;

--
-- AUTO_INCREMENT for table `comments`
--
ALTER TABLE `comments`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;

--
-- AUTO_INCREMENT for table `data_badges`
--
ALTER TABLE `data_badges`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=29;

--
-- AUTO_INCREMENT for table `event`
--
ALTER TABLE `event`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `giftcode`
--
ALTER TABLE `giftcode`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `history_items_diemdanh`
--
ALTER TABLE `history_items_diemdanh`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=95;

--
-- AUTO_INCREMENT for table `history_transaction`
--
ALTER TABLE `history_transaction`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=46;

--
-- AUTO_INCREMENT for table `hist_quydoitv`
--
ALTER TABLE `hist_quydoitv`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `img_by_name`
--
ALTER TABLE `img_by_name`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=318;

--
-- AUTO_INCREMENT for table `mbbank`
--
ALTER TABLE `mbbank`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `moc_capsule_trang_suc`
--
ALTER TABLE `moc_capsule_trang_suc`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=20;

--
-- AUTO_INCREMENT for table `moc_hopqua2010`
--
ALTER TABLE `moc_hopqua2010`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=20;

--
-- AUTO_INCREMENT for table `moc_nap`
--
ALTER TABLE `moc_nap`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=17;

--
-- AUTO_INCREMENT for table `moc_nap_top`
--
ALTER TABLE `moc_nap_top`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=20;

--
-- AUTO_INCREMENT for table `moc_san_boss`
--
ALTER TABLE `moc_san_boss`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=17;

--
-- AUTO_INCREMENT for table `moc_suc_manh`
--
ALTER TABLE `moc_suc_manh`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=17;

--
-- AUTO_INCREMENT for table `moc_suc_manh_top`
--
ALTER TABLE `moc_suc_manh_top`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=20;

--
-- AUTO_INCREMENT for table `moc_thiepchucvip`
--
ALTER TABLE `moc_thiepchucvip`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=21;

--
-- AUTO_INCREMENT for table `napthe`
--
ALTER TABLE `napthe`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `naptien`
--
ALTER TABLE `naptien`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `notify`
--
ALTER TABLE `notify`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT for table `payments`
--
ALTER TABLE `payments`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `player`
--
ALTER TABLE `player`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=18;

--
-- AUTO_INCREMENT for table `posts`
--
ALTER TABLE `posts`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=93;

--
-- AUTO_INCREMENT for table `power_limit`
--
ALTER TABLE `power_limit`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;

--
-- AUTO_INCREMENT for table `recharge_log`
--
ALTER TABLE `recharge_log`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `shop`
--
ALTER TABLE `shop`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=53;

--
-- AUTO_INCREMENT for table `tab_shop`
--
ALTER TABLE `tab_shop`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=77;

--
-- AUTO_INCREMENT for table `task_badges_template`
--
ALTER TABLE `task_badges_template`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;

--
-- AUTO_INCREMENT for table `task_sub_template`
--
ALTER TABLE `task_sub_template`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=1000;

--
-- AUTO_INCREMENT for table `transaction_banking`
--
ALTER TABLE `transaction_banking`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `trans_log`
--
ALTER TABLE `trans_log`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `type_item`
--
ALTER TABLE `type_item`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=82;

--
-- AUTO_INCREMENT for table `type_sell_item_shop`
--
ALTER TABLE `type_sell_item_shop`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `zalo_groups`
--
ALTER TABLE `zalo_groups`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `zalo_users`
--
ALTER TABLE `zalo_users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=200;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `comments`
--
ALTER TABLE `comments`
  ADD CONSTRAINT `comments_ibfk_1` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`);

--
-- Constraints for table `player`
--
ALTER TABLE `player`
  ADD CONSTRAINT `player_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`);

--
-- Constraints for table `shop`
--
ALTER TABLE `shop`
  ADD CONSTRAINT `shop_ibfk_1` FOREIGN KEY (`npc_id`) REFERENCES `npc_template` (`id`);

--
-- Constraints for table `tab_shop`
--
ALTER TABLE `tab_shop`
  ADD CONSTRAINT `tab_shop_ibfk_1` FOREIGN KEY (`shop_id`) REFERENCES `shop` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
