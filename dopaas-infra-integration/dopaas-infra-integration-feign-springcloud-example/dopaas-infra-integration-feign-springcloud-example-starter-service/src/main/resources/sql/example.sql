DROP TABLE IF EXISTS `t_example_order`;
CREATE TABLE `t_example_order`  (
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

INSERT INTO `t_example_order` (`id`, `name`, `order_no`, `delivery_address`, `organization_code`, `enable`, `remark`, `create_by`, `create_date`, `update_by`, `update_date`, `del_flag`) VALUES (10001, 'Sniper rifle', 202100121, '1458 Bee Street1', 'abcd111111', 1, NULL, 1, '2021-05-20 15:25:07', 1, '2021-05-20 15:24:15', 0);
INSERT INTO `t_example_order` (`id`, `name`, `order_no`, `delivery_address`, `organization_code`, `enable`, `remark`, `create_by`, `create_date`, `update_by`, `update_date`, `del_flag`) VALUES (10002, 'Over limit combat check', 202100122, '95 Oxford Rd', 'abcd122222', 1, NULL, 1, '2021-05-20 15:25:07', 1, '2021-05-20 15:24:15', 0);
INSERT INTO `t_example_order` (`id`, `name`, `order_no`, `delivery_address`, `organization_code`, `enable`, `remark`, `create_by`, `create_date`, `update_by`, `update_date`, `del_flag`) VALUES (10003, 'fake vote', 202100123, '394 Patterson Fork Road', 'abcd133333', 1, NULL, 1, '2021-05-20 15:25:07', 1, '2021-05-20 15:24:15', 0);
INSERT INTO `t_example_order` (`id`, `name`, `order_no`, `delivery_address`, `organization_code`, `enable`, `remark`, `create_by`, `create_date`, `update_by`, `update_date`, `del_flag`) VALUES (10004, 'jacks1', 202100124, '401 Patterson Asd', 'abcd14444', 1, NULL, 1, '2021-05-20 15:25:07', 1, '2021-05-20 15:24:15', 0);
