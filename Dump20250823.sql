-- MySQL dump 10.13  Distrib 8.0.42, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: livsi
-- ------------------------------------------------------
-- Server version	8.0.42

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `member`
--

DROP TABLE IF EXISTS `member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `member` (
  `member_id` bigint NOT NULL AUTO_INCREMENT,
  `password` varchar(40) COLLATE utf8mb4_0900_as_ci NOT NULL,
  `member_name` varchar(10) COLLATE utf8mb4_0900_as_ci NOT NULL,
  `email` varchar(40) COLLATE utf8mb4_0900_as_ci NOT NULL,
  PRIMARY KEY (`member_id`),
  UNIQUE KEY `email_UNIQUE` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `region`
--

DROP TABLE IF EXISTS `region`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `region` (
  `region_id` bigint NOT NULL AUTO_INCREMENT,
  `sido` varchar(300) NOT NULL,
  `sigungu` varchar(300) NOT NULL,
  `mascot_img` tinyblob,
  PRIMARY KEY (`region_id`),
  UNIQUE KEY `district_UNIQUE` (`sigungu`),
  UNIQUE KEY `UQ_city_district` (`sido`,`sigungu`),
  KEY `idx_region_city_district` (`sido`,`sigungu`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `store`
--

DROP TABLE IF EXISTS `store`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `store` (
  `store_id` bigint NOT NULL AUTO_INCREMENT,
  `store_name` varchar(100) NOT NULL,
  `store_address` varchar(100) DEFAULT NULL,
  `sido` varchar(300) DEFAULT NULL,
  `sigungu` varchar(300) DEFAULT NULL,
  `region_id` bigint DEFAULT NULL,
  PRIMARY KEY (`store_id`),
  KEY `fk_store_region_idx` (`sido`,`sigungu`),
  KEY `FKiecbc1b9m21semcf714lasyi5` (`region_id`),
  CONSTRAINT `fk_store_region` FOREIGN KEY (`sido`, `sigungu`) REFERENCES `region` (`sido`, `sigungu`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKiecbc1b9m21semcf714lasyi5` FOREIGN KEY (`region_id`) REFERENCES `region` (`region_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `video`
--

DROP TABLE IF EXISTS `video`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `video` (
  `video_id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint NOT NULL,
  `store_id` bigint NOT NULL,
  `sido` varchar(300) NOT NULL,
  `sigungu` varchar(300) NOT NULL,
  `title` varchar(100) NOT NULL,
  `video_url` text NOT NULL,
  `thumbnail_url` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `sido_id` bigint DEFAULT NULL,
  `sigungu_id` bigint DEFAULT NULL,
  `description` varchar(200) DEFAULT NULL,
  `region_id` bigint DEFAULT NULL,
  PRIMARY KEY (`video_id`),
  KEY `member_id_idx` (`member_id`),
  KEY `fk_video_region_idx` (`sido`,`sigungu`),
  KEY `store_id_idx` (`store_id`),
  KEY `FK5stua8jt317mrj4rkiek65xdr` (`region_id`),
  KEY `FK36yk12k4fecnlt0ckky6kgmf1` (`sido_id`),
  KEY `FK6rytnr07u6l72gqm2fpd7kkhp` (`sigungu_id`),
  CONSTRAINT `FK36yk12k4fecnlt0ckky6kgmf1` FOREIGN KEY (`sido_id`) REFERENCES `region` (`region_id`),
  CONSTRAINT `FK5stua8jt317mrj4rkiek65xdr` FOREIGN KEY (`region_id`) REFERENCES `region` (`region_id`),
  CONSTRAINT `FK6rytnr07u6l72gqm2fpd7kkhp` FOREIGN KEY (`sigungu_id`) REFERENCES `region` (`region_id`),
  CONSTRAINT `fk_video_region` FOREIGN KEY (`sido`, `sigungu`) REFERENCES `region` (`sido`, `sigungu`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `member_id` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `store_id` FOREIGN KEY (`store_id`) REFERENCES `store` (`store_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-08-23 21:26:17
