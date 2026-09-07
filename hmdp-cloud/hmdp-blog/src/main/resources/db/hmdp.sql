-- H2 Database Schema for HMDP
-- Compatible with H2 in MySQL mode

-- Table structure for tb_blog
DROP TABLE IF EXISTS tb_blog;
CREATE TABLE tb_blog (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  shop_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  title VARCHAR(255) NOT NULL,
  images VARCHAR(2048) NOT NULL,
  content VARCHAR(2048) NOT NULL,
  liked INT DEFAULT 0,
  comments INT DEFAULT 0,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table structure for tb_blog_comments
DROP TABLE IF EXISTS tb_blog_comments;
CREATE TABLE tb_blog_comments (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  blog_id BIGINT NOT NULL,
  parent_id BIGINT NOT NULL,
  answer_id BIGINT NOT NULL,
  content VARCHAR(255) NOT NULL,
  liked INT DEFAULT 0,
  status TINYINT DEFAULT 0,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table structure for tb_follow
DROP TABLE IF EXISTS tb_follow;
CREATE TABLE tb_follow (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  follow_user_id BIGINT NOT NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table structure for tb_seckill_voucher
DROP TABLE IF EXISTS tb_seckill_voucher;
CREATE TABLE tb_seckill_voucher (
  voucher_id BIGINT PRIMARY KEY,
  stock INT NOT NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  begin_time TIMESTAMP DEFAULT '1970-01-01 00:00:00',
  end_time TIMESTAMP DEFAULT '1970-01-01 00:00:00',
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table structure for tb_shop
DROP TABLE IF EXISTS tb_shop;
CREATE TABLE tb_shop (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(128) NOT NULL,
  type_id BIGINT NOT NULL,
  images VARCHAR(1024) NOT NULL,
  area VARCHAR(128),
  address VARCHAR(255) NOT NULL,
  x DOUBLE NOT NULL,
  y DOUBLE NOT NULL,
  avg_price BIGINT,
  sold INT NOT NULL DEFAULT 0,
  comments INT NOT NULL DEFAULT 0,
  score INT NOT NULL DEFAULT 0,
  open_hours VARCHAR(32),
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table structure for tb_shop_type
DROP TABLE IF EXISTS tb_shop_type;
CREATE TABLE tb_shop_type (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(32),
  icon VARCHAR(255),
  sort INT,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table structure for tb_user
DROP TABLE IF EXISTS tb_user;
CREATE TABLE tb_user (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  phone VARCHAR(11) NOT NULL UNIQUE,
  password VARCHAR(128),
  nick_name VARCHAR(32),
  icon VARCHAR(255),
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table structure for tb_user_info
DROP TABLE IF EXISTS tb_user_info;
CREATE TABLE tb_user_info (
  user_id BIGINT PRIMARY KEY,
  city VARCHAR(32),
  introduce VARCHAR(255),
  fans INT DEFAULT 0,
  followee INT DEFAULT 0,
  gender TINYINT DEFAULT 0,
  birthday DATE,
  credits INT DEFAULT 0,
  level TINYINT DEFAULT 0,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table structure for tb_voucher
DROP TABLE IF EXISTS tb_voucher;
CREATE TABLE tb_voucher (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  shop_id BIGINT,
  title VARCHAR(255) NOT NULL,
  sub_title VARCHAR(255),
  rules VARCHAR(1024),
  pay_value BIGINT NOT NULL,
  actual_value BIGINT NOT NULL,
  type TINYINT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table structure for tb_voucher_order
DROP TABLE IF EXISTS tb_voucher_order;
CREATE TABLE tb_voucher_order (
  id BIGINT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  voucher_id BIGINT NOT NULL,
  pay_type TINYINT NOT NULL DEFAULT 1,
  status TINYINT NOT NULL DEFAULT 1,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  pay_time TIMESTAMP,
  use_time TIMESTAMP,
  refund_time TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert sample data for tb_shop_type
INSERT INTO tb_shop_type (name, icon, sort) VALUES 
('美食', '/types/ms.png', 1),
('KTV', '/types/KTV.png', 2),
('酒店', '/types/jd.png', 3),
('足浴', '/types/zy.png', 4),
('按摩', '/types/am.png', 5);

-- Insert sample data for tb_shop
INSERT INTO tb_shop (name, type_id, images, area, address, x, y, avg_price, sold, comments, score, open_hours) VALUES 
('103茶餐厅', 1, 'https://qcloud.dpfile.com/pc/jiclIsCKmOI2arxKN1Uf0Hx3PucIJH8q0QSz-Z8llzcN56-_QiKuOvyio1OOxsRtFoXqu0G3iT2T27qat3WhLVEuLYk00OmSS1IdNpm8K8sG4JN9RIm2mTKcbLtc2o2vfCF2ubeXzk49OsGrXt_KYDCngOyCwZK-s3fqawWswzk.jpg', '大关', '金华路锦昌文华苑29号', 120.149192, 30.316078, 80, 4215, 3035, 37, '10:00-22:00'),
('蔡馬洪涛烤肉·老北京铜锅涮羊肉', 1, 'https://p0.meituan.net/bbia/c1870d570e73accbc9fee90b48faca41195272.jpg', '拱宸桥/上塘', '上塘路1035号（中国工商银行旁）', 120.151505, 30.333422, 85, 2160, 1460, 46, '11:30-03:00'),
('新白鹿餐厅(运河上街店)', 1, 'https://p0.meituan.net/biztone/694233_1619500156517.jpeg', '运河上街', '台州路2号运河上街购物中心F5', 120.151954, 30.32497, 61, 12035, 8045, 47, '10:30-21:00');

-- Insert sample data for tb_user
INSERT INTO tb_user (phone, nick_name, icon) VALUES 
('13800138000', '测试用户1', '/imgs/icons/user1.jpg'),
('13800138001', '测试用户2', '/imgs/icons/user2.jpg');

-- Insert sample data for tb_blog
INSERT INTO tb_blog (shop_id, user_id, title, images, content, liked, comments) VALUES 
(1, 1, '美味的茶餐厅体验', '/imgs/blogs/1/2/38f7e67d-7b26-41fd-bcf5-eebf71914230.jpg', '今天去了103茶餐厅，味道很不错！', 5, 2),
(2, 2, '烤肉店推荐', '/imgs/blogs/2/1/7b31a5da-4ee3-4c9e-a38a-2dbc8de0345a.jpg', '蔡馬洪涛烤肉真的很棒，推荐大家去试试！', 8, 3),
(3, 1, '新白鹿餐厅体验', '/imgs/blogs/1/2/4a7b496b-2a08-4af7-aa95-df2c3bd0ef97.jpg', '新白鹿餐厅的菜品很丰富，环境也不错！', 12, 5),
(1, 2, '再次光顾茶餐厅', '/imgs/blogs/2/2/7fc130a4-67b7-4339-9626-9680b198d9a0.jpg', '第二次来103茶餐厅，依然很满意！', 7, 1),
(2, 1, '烤肉配涮羊肉', '/imgs/blogs/1/2/f052df92-fc05-41a6-866e-d6da63b27645.jpg', '烤肉和涮羊肉的组合真是绝了！', 15, 8);

-- ============ 新增功能表 (WorkBuddy extension) ============

-- Table structure for tb_shop_score (sub-ratings: taste/environment/service)
DROP TABLE IF EXISTS tb_shop_score;
CREATE TABLE tb_shop_score (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  shop_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  taste INT NOT NULL,
  environment INT NOT NULL,
  service INT NOT NULL,
  content VARCHAR(512),
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table structure for tb_user_favorite (wishlist)
DROP TABLE IF EXISTS tb_user_favorite;
CREATE TABLE tb_user_favorite (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  shop_id BIGINT NOT NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table structure for tb_message (消息中心)
DROP TABLE IF EXISTS tb_message;
CREATE TABLE tb_message (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  type INT NOT NULL DEFAULT 1,
  content VARCHAR(512) NOT NULL,
  target_id BIGINT,
  is_read TINYINT DEFAULT 0,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
INSERT INTO tb_message (user_id, type, content, target_id) VALUES
(1, 2, '欢迎来到小红碗点评，开始你的探店之旅吧！', NULL),
(1, 1, '测试用户2 赞了你的笔记「烤肉配涮羊肉」', 5),
(2, 1, '测试用户1 赞了你的笔记「美味的茶餐厅体验」', 1);
