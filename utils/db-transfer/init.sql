-- mysql -u root -h 192.168.100.161 --default-character-set=utf8 -pxxxxx reeb < init.sql


--
-- 转存表中的数据 `operate_users`
--

-- INSERT INTO `operate_users` (`id`, `created_at`, `deleted`, `encrypted_password`, `last_login_at`, `last_login_ip`, `lock_version`, `login_name`, `mobile`, `password_salt`, `updated_at`, `user_name`) VALUES
-- (1, '2012-04-10 10:58:02', 0, '1360dceeb96ae52402fb21f954a8268d', '2012-05-15 12:42:41', '0:0:0:0:0:0:0:1', 0, 'admin', '13910001000', 'mSKHrC', '2012-05-15 12:42:41', '管理员');

-- 加入角色
-- INSERT INTO `operate_users_roles` (`role_id`, `user_id`) SELECT `id`, 1 FROM  `operate_roles` ;


--
-- 转存表中的数据 `user_point_config`
--

delete from `user_point_config`;

INSERT INTO `user_point_config` (`id`, `point_type`, `point_number`, `deal_points`, `point_note`, `point_title`) VALUES
(1, 1, '100', 200, '成功注册获得积分', '注册'),
(2, 1, '101', 100, '补充完整会员资料获得积分', '补充完资料'),
(3, 1, '102', 100, '用户每天登录网站/每天只算一次', '登录'),
(4, 1, '103', 0, '用户每月第一笔充值增送积分', '充值'),
(5, 1, '104', 10, '网站或终端打印优惠券，每天计算10张', '打印优惠券/短信优惠券'),
(6, 1, '105', 0, '自由的给商户的评论，每天计算10条', '普通商户评论'),
(7, 1, '106', 0, '给商户上传的图片，每天计算10条', '上传图片'),
(8, 1, '107', 0, '购买消费后给商户的评论', '消费给商户评论'),
(9, 1, '109', 10, '1元获得1积分，优卡会员双倍积分', '购买商品'),
(10, 1, '110', 10, '1元获得1积分，优卡会员双倍积分', '团购'),
(11, 0, '120', 0, '删除图片扣除积分', '删除图片'),
(12, 0, '121', 0, '删除评论扣除积分', '删除评论'),
(13, 1, '122', 2000, '邀请积分', '邀请积分'),
(14, 1, '123', 10, '退款', '退款'),
(15, 1, '124', 1000, '会员帐号绑定手机获得积分, 只计算第一次', '绑定手机'),
(16, 1, '125', 500, '会员帐号绑定邮箱优卡获得积分, 只计算第一次', '绑定邮箱'),
(17, 1, '126', 2000, '会员帐号优卡获得积分，只计算第一次(首次)', '绑定优卡');


--
-- 转存表中的数据 `areas`
--
update `areas` set parent_id=null;
delete from `areas`;

INSERT INTO `areas` (`id`, `area_type`, `display_order`, `name`, `parent_id`) VALUES
('021', 'CITY', 100, '上海', NULL),
('02101', 'DISTRICT', 100, '卢湾区', '021'),
('02101001', 'AREA', 100, '淮海路', '02101'),
('02101002', 'AREA', 200, '新天地', '02101'),
('02101003', 'AREA', 1030, '瑞金宾馆区', '02101'),
('02101004', 'AREA', 1040, '打浦桥', '02101'),
('02102', 'DISTRICT', 200, '徐汇区', '021'),
('02102001', 'AREA', 300, '徐家汇', '02102'),
('02102002', 'AREA', 2020, '万体馆', '02102'),
('02102003', 'AREA', 2030, '衡山路', '02102'),
('02102004', 'AREA', 2040, '复兴西路', '02102'),
('02102005', 'AREA', 2050, '丁香花园', '02102'),
('02102006', 'AREA', 2060, '肇嘉浜沿线', '02102'),
('02102007', 'AREA', 2070, '音乐学院', '02102'),
('02102008', 'AREA', 2080, '龙华', '02102'),
('02102009', 'AREA', 2090, '漕河泾', '02102'),
('0210210', 'AREA', 2100, '田林', '02102'),
('0210211', 'AREA', 2110, '上海南站', '02102'),
('02103', 'DISTRICT', 300, '静安区', '021'),
('02103001', 'AREA', 3010, '南京西路', '02103'),
('02103002', 'AREA', 3020, '静安寺', '02103'),
('02103003', 'AREA', 3030, '曹家渡', '02103'),
('02103004', 'AREA', 3040, '同乐坊', '02103'),
('02103005', 'AREA', 3050, '吴江路', '02103'),
('02104', 'DISTRICT', 400, '长宁区', '021'),
('02104001', 'AREA', 4010, '中山公园', '02104'),
('02104002', 'AREA', 4020, '虹桥', '02104'),
('02104003', 'AREA', 4030, '天山', '02104'),
('02104004', 'AREA', 4040, '古北', '02104'),
('02104005', 'AREA', 4050, '上海影城', '02104'),
('02104006', 'AREA', 4060, '新华路', '02104'),
('02104007', 'AREA', 4070, '动物园', '02104'),
('02104008', 'AREA', 4080, '虹桥机场', '02104'),
('02104009', 'AREA', 4090, '北新泾', '02104'),
('0210410', 'AREA', 4100, '虹桥火车站', '02104'),
('02105', 'DISTRICT', 500, '闵行区', '021'),
('02105001', 'AREA', 5010, '虹桥镇', '02105'),
('02105002', 'AREA', 5020, '虹梅路', '02105'),
('02105003', 'AREA', 5030, '七宝', '02105'),
('02105004', 'AREA', 5040, '莘庄', '02105'),
('02105005', 'AREA', 5050, '南方商城', '02105'),
('02105006', 'AREA', 5060, '春申地区', '02105'),
('02105007', 'AREA', 5070, '老闵行', '02105'),
('02105008', 'AREA', 5080, ' 万源城', '02105'),
('02105009', 'AREA', 5090, '东兰路', '02105'),
('0210510', 'AREA', 5100, '龙柏地区', '02105'),
('0210511', 'AREA', 5110, '吴中路', '02105'),
('0210512', 'AREA', 5120, '颛桥', '02105'),
('02106', 'DISTRICT', 600, '浦东', '021'),
('02106001', 'AREA', 6010, '陆家嘴', '02106'),
('02106002', 'AREA', 6020, '八佰伴', '02106'),
('02106003', 'AREA', 6030, '世纪公园', '02106'),
('02106004', 'AREA', 6040, '上南地区', '02106'),
('02106005', 'AREA', 6050, '外高桥', '02106'),
('02106006', 'AREA', 6060, '金桥', '02106'),
('02106007', 'AREA', 6070, '源深体育中心', '02106'),
('02106008', 'AREA', 6080, '张江', '02106'),
('02106009', 'AREA', 6090, '塘桥', '02106'),
('0210610', 'AREA', 6100, '川沙', '02106'),
('0210611', 'AREA', 6110, '三林镇', '02106'),
('0210612', 'AREA', 6120, '碧云社区', '02106'),
('0210613', 'AREA', 6130, '金杨地区', '02106'),
('0210614', 'AREA', 6140, '康桥', '02106'),
('0210615', 'AREA', 6150, '周浦', '02106'),
('0210616', 'AREA', 6160, '惠南镇', '02106'),
('02107', 'DISTRICT', 700, '黄浦区', '021'),
('02107001', 'AREA', 7010, '外滩', '02107'),
('02107002', 'AREA', 7020, '人民广场', '02107'),
('02107003', 'AREA', 7030, '南京东路', '02107'),
('02107004', 'AREA', 7040, '城隍庙', '02107'),
('02107005', 'AREA', 7050, '老西门', '02107'),
('02107006', 'AREA', 7060, '董家渡', '02107'),
('02108', 'DISTRICT', 800, '普陀区', '021'),
('02108001', 'AREA', 8010, '真如', '02108'),
('02108002', 'AREA', 8020, '武宁地区', '02108'),
('02108003', 'AREA', 8030, '长寿路', '02108'),
('02108004', 'AREA', 8040, '长风公园', '02108'),
('02108005', 'AREA', 8050, '华师大', '02108'),
('02108006', 'AREA', 8060, '曹杨地区', '02108'),
('02108007', 'AREA', 8070, '梅川路', '02108'),
('02108008', 'AREA', 8080, '中山北路', '02108'),
('02108009', 'AREA', 8090, '甘泉地区', '02108'),
('0210810', 'AREA', 8100, ' 曹家渡', '02108'),
('0210811', 'AREA', 8110, '大自鸣钟广场', '02108'),
('02109', 'DISTRICT', 900, '闸北区', '021'),
('02109001', 'AREA', 9010, '火车站', '02109'),
('02109002', 'AREA', 9020, '大宁地区', '02109'),
('02109003', 'AREA', 9030, '彭浦新村', '02109'),
('02109004', 'AREA', 9040, '闸北公园', '02109'),
('02109005', 'AREA', 9050, '北区汽车站', '02109'),
('02109006', 'AREA', 9060, '新客站', '02109'),
('02110', 'DISTRICT', 1000, '虹口区', '021'),
('02110001', 'AREA', 10010, '曲阳地区', '02110'),
('02110002', 'AREA', 10020, '鲁迅公园', '02110'),
('02110003', 'AREA', 10030, '四川北路', '02110'),
('02110004', 'AREA', 10040, '海宁路', '02110'),
('02110005', 'AREA', 10050, '七浦路', '02110'),
('02110006', 'AREA', 10060, '临平路', '02110'),
('02110007', 'AREA', 10070, '和平公园', '02110'),
('02110008', 'AREA', 10080, '北外滩', '02110'),
('02110009', 'AREA', 10090, '凉城', '02110'),
('0211010', 'AREA', 10100, '江湾镇', '02110'),
('0211011', 'AREA', 10110, '大柏树', '02110'),
('02111', 'DISTRICT', 1100, '杨浦区', '021'),
('02111001', 'AREA', 11010, '五角场', '02111'),
('02111002', 'AREA', 11020, '大学区', '02111'),
('02111003', 'AREA', 11030, '控江地区', '02111'),
('02111004', 'AREA', 11040, '中原地区', '02111'),
('02111005', 'AREA', 11050, '黄兴公园', '02111'),
('02111006', 'AREA', 11060, '平凉路', '02111'),
('02111007', 'AREA', 11070, '鞍山路', '02111'),
('02112', 'DISTRICT', 1200, '宝山区', '021'),
('02112001', 'AREA', 12010, '大华地区', '02112'),
('02112002', 'AREA', 12020, '吴淞', '02112'),
('02112003', 'AREA', 12030, '庙行镇', '02112'),
('02112004', 'AREA', 12040, '上海大学', '02112'),
('02112005', 'AREA', 12050, '共康', '02112'),
('02112006', 'AREA', 12060, '友谊路', '02112'),
('02112007', 'AREA', 12070, '长江西路', '02112'),
('02113', 'DISTRICT', 1300, '近郊', '021'),
('02113001', 'AREA', 13010, '奉贤', '02113'),
('02113002', 'AREA', 13020, '金山', '02113'),
('02113003', 'AREA', 13030, '崇明', '02113'),
('02113004', 'AREA', 13040, '松江区', '02113'),
('02113005', 'AREA', 13050, '嘉定区', '02113'),
('02113006', 'AREA', 13060, '青浦区', '02113');

--
-- 转存表中的数据 `payment_source`
--
-- delete from `payment_source`;
-- INSERT INTO `payment_source` (`id`, `code`, `detail`, `logo`, `name`, `payment_code`, `show_order`) VALUES
-- (1, 'alipay', '支付宝', 'ali', '支付宝', 'alipay', 1),
-- (2, 'tenpay', '财付通', 'tenpay', '财付通', 'tenpay', 2),
-- (3, 'CMB', '招商银行', 'images/bank/bank_cmb.gif', '招商银行', '99bill', 3),
-- (4, 'ABC', '农业银行', 'images/bank/bank_abc.gif', '农业银行', '99bill', 4),
-- (5, 'balance', '余额支付', '把我的show_order设置为0吧亲', '余额支付', 'balance', 0),
-- (6, 'BCOM', '交通银行', 'images/bank/bank_bcom.gif', '交通银行', '99bill', 5),
-- (7, 'BEA', 'BEA东亚银行', 'images/bank/bank_bea.gif', 'BEA东亚银行', '99bill', 6),
-- (8, 'BJRCB', '北京农村商业银行', 'images/bank/bank_bjrcb.gif', '北京农村商业银行', '99bill', 7),
-- (9, 'BOB', '北京银行', 'images/bank/bank_bob.gif', '北京银行', '99bill', 8),
-- (10, 'BOC', '中国银行', 'images/bank/bank_boc.gif', '中国银行', '99bill', 9),
-- (11, 'CBHB', '渤海银行', 'images/bank/bank_cbhb.gif', '渤海银行', '99bill', 10),
-- (12, 'CCB', '中国建设银行', 'images/bank/bank_ccb.gif', '中国建设银行', '99bill', 11),
-- (13, 'CEB', '中国光大银行', 'images/bank/bank_ceb.gif', '中国光大银行', '99bill', 12),
-- (14, 'CIB', '兴业银行', 'images/bank/bank_cib.gif', '兴业银行', '99bill', 13),
-- (15, 'CITIC', '中信银行', 'images/bank/bank_citic.gif', '中信银行', '99bill', 14),
-- (16, 'CMBC', '中国民生银行', 'images/bank/bank_cmbc.gif', '中国民生银行', '99bill', 15),
-- (17, 'CZB', '浙商银行', 'images/bank/bank_czb.gif', '浙商银行', '99bill', 16),
-- (18, 'GDB', '广东发展银行', 'images/bank/bank_gdb.gif', '广东发展银行', '99bill', 17),
-- (19, 'GZCB', '广州市商业银行', 'images/bank/bank_gzcb.gif', '广州市商业银行', '99bill', 18),
-- (20, 'GZRCC', '广州市农村信用合作社', 'images/bank/bank_gzrcc.gif', '广州市农村信用合作社', '99bill', 19),
-- (21, 'HSB', '徽商银行', 'images/bank/bank_hsb.gif', '徽商银行', '99bill', 20),
-- (22, 'HXB', '华夏银行', 'images/bank/bank_hxb.gif', '华夏银行', '99bill', 21),
-- (23, 'HZB', '杭州银行', 'images/bank/bank_hzb.gif', '杭州银行', '99bill', 22),
-- (24, 'ICBC', '中国工商银行', 'images/bank/bank_icbc.gif', '中国工商银行', '99bill', 23),
-- (25, 'NBCB', '宁波银行', 'images/bank/bank_nbcb.gif', '宁波银行', '99bill', 24),
-- (26, 'NJCB', '南京银行', 'images/bank/bank_njcb.gif', '南京银行', '99bill', 25),
-- (27, 'PAB', '平安银行', 'images/bank/bank_pab.gif', '平安银行', '99bill', 26),
-- (28, 'POST', '中国邮政', 'images/bank/bank_post.gif', '中国邮政', '99bill', 27),
-- (29, 'SDB', '深圳发展银行', 'images/bank/bank_sdb.gif', '深圳发展银行', '99bill', 28),
-- (30, 'SHB', '上海银行', 'images/bank/bank_shb.gif', '上海银行', '99bill', 29),
-- (31, 'SHRCC', '上海农村商业银行', 'images/bank/bank_shrcc.gif', '上海农村商业银行', '99bill', 30),
-- (32, 'SPDB', '浦发银行', 'images/bank/bank_spdb.gif', '浦发银行', '99bill', 31);

--
-- 转存表中的数据 `categories`
--
-- update `categories` set `parent_id`=null;
-- delete from `categories`;
-- INSERT INTO `categories` (`id`, `display_order`, `name`, `parent_id`) VALUES
-- (1, 100, '美食券', NULL),
-- (2, 200, '食品券', NULL),
-- (3, 300, '川菜', 1),
-- (4, 400, '快餐', 1),
-- (5, 500, '面包甜点', 2),
-- (6, 600, '休闲食品', 2);