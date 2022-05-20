DROP TABLE IF EXISTS `t_order`;
CREATE TABLE `t_order`  (
  `id` bigint(25) PRIMARY KEY NOT NULL,
  `name` varchar(32) NOT NULL,
  `order_no` int(11) NULL,
  `delivery_address` VARCHAR(255) NULL,
  `organization_code` VARCHAR(32) NULL,
  `enable` int(1) NOT NULL,
  `remark` varchar(255) NULL,
  `create_by` bigint(25) NOT NULL,
  `create_date` TIMESTAMP NOT NULL,
  `update_by` bigint(25) NOT NULL,
  `update_date` TIMESTAMP NOT NULL,
  `del_flag` int(1) NOT NULL
);
