/**
 * 虚拟评论数据 — 按 room_type_id 分组
 * 房型 1-7 各约 10 条，内容不同，部分虚拟用户出现在多个房型
 */
const mockReviews = {
  1: [
    { nickname: '背包客阿明', avatar: '🎒', content: '麻雀虽小五脏俱全，一个人住刚刚好，性价比超高！下次来还会选这个房型。' },
    { nickname: '出差达人', avatar: '💼', content: '出差首选，价格实惠，房间干净整洁。WiFi速度也不错，办公没压力。' },
    { nickname: '文艺青年M', avatar: '🎨', content: '小而美的空间，窗外的光线很好，适合一个人安静地看书发呆。' },
    { nickname: '城市探索者', avatar: '🏃', content: '大部分时间在外面探索城市，回酒店就是睡个觉，这个房型完全够用。' },
    { nickname: '学生党小李', avatar: '📚', content: '价格对学生来说很友好，环境也不错。推荐给预算有限的朋友们。' },
    { nickname: '说走就走', avatar: '✈️', content: '临时订的房，没想到体验这么好。小而精致，服务也很到位。' },
    { nickname: '独行侠老张', avatar: '🚶', content: '一个人旅行多年，就喜欢这种简单舒服的房间。床品质量不错。' },
    { nickname: '酒店控Linda', avatar: '🏨', content: '虽然是最基础的房型，但品质保持得不错。热水很足，空调很给力。' },
    { nickname: '摄影爱好者', avatar: '📷', content: '房间光线很适合拍照！小而美，北欧简约风，随手一拍都好看。' },
    { nickname: '商务张先生', avatar: '👔', content: '短期出差住了一晚，该有的都有，前台服务热情，下次还会来。' },
  ],
  2: [
    { nickname: '蜜月情侣', avatar: '💑', content: '和女朋友一起住的，房间温馨浪漫，大床很舒服，睡得很好。' },
    { nickname: '商务张先生', avatar: '👔', content: '标准房型，配置齐全。交通方便是最大优势，离地铁站很近。' },
    { nickname: '酒店控Linda', avatar: '🏨', content: '标准大床房的品质对得起这个价位，装修风格很有品味。推荐！' },
    { nickname: '周末度假族', avatar: '🌴', content: '周末来放松，床品超赞！一觉睡到自然醒，都不想退房了。' },
    { nickname: '旅行者小王', avatar: '🧳', content: '房间比想象中大，布置得很温馨。前台小姐姐推荐了附近好吃的店。' },
    { nickname: '文艺青年M', avatar: '🎨', content: '简约不简单，细节做得很用心。桌上的绿植让人心情很好。' },
    { nickname: '出差达人', avatar: '💼', content: '第二次入住了，品质一如既往地稳定。办公桌够大，适合加班。' },
    { nickname: '吃货一枚', avatar: '🍜', content: '酒店附近好吃的太多了！房间住着也舒服，吃饱了回去躺着太幸福了。' },
    { nickname: '睡到自然醒', avatar: '😴', content: '隔音效果不错，床垫软硬适中，对于睡眠质量要求高的我很满意。' },
    { nickname: '悠闲慢生活', avatar: '🌿', content: '整体体验很好，不急不躁的氛围。适合想要慢下来享受生活的人。' },
  ],
  3: [
    { nickname: '蜜月情侣', avatar: '💑', content: '升级到高级房，空间大了不少，早餐也很丰富。纪念日选了这里太对了！' },
    { nickname: '商务张先生', avatar: '👔', content: '高级房的体验明显上一个档次，会客区很实用，客户来了也有面子。' },
    { nickname: '酒店控Linda', avatar: '🏨', content: '这个房型的装修细节真的赞，每一处都能感受到用心。含早很值。' },
    { nickname: '周末度假族', avatar: '🌴', content: '早餐选择很多，中西式都有。房间宽敞明亮，很适合宅一整天。' },
    { nickname: '旅行者小王', avatar: '🧳', content: '性价比很高的高级房！面积够大，还有浴缸，泡个澡太解乏了。' },
    { nickname: '亲子家庭', avatar: '👨‍👩‍👧', content: '虽然是高级大床房不是亲子房，但是加了个婴儿床也不挤，服务很贴心。' },
    { nickname: '悠闲慢生活', avatar: '🌿', content: '床品比标准房升级了不少，枕头还有菜单可以选，细节满分。' },
    { nickname: '摄影爱好者', avatar: '📷', content: '房间的采光和设计都非常适合拍照，发朋友圈直接被问爆了。' },
    { nickname: '文艺青年M', avatar: '🎨', content: '高级房的艺术装饰品很有格调，住着就是一种享受。' },
    { nickname: '睡到自然醒', avatar: '😴', content: '遮光窗帘+舒适的床+安静的环境=完美的睡眠体验。强烈推荐。' },
  ],
  4: [
    { nickname: '出差达人', avatar: '💼', content: '和同事一起出差，双床房是最佳选择。每人一张床，互不打扰。' },
    { nickname: '旅行者小王', avatar: '🧳', content: '和朋友出游订的双床房，两床之间距离合适，大家都有独立空间。' },
    { nickname: '商务张先生', avatar: '👔', content: '双床房也有办公桌，出差办公两不误。早餐种类挺多的。' },
    { nickname: '文艺青年M', avatar: '🎨', content: '和闺蜜出游住这里，两张床都很舒服，房间配色也很温馨~' },
    { nickname: '说走就走', avatar: '✈️', content: '临时出差订到的双床房，运气不错。整体干净整洁，标准很高。' },
    { nickname: '学生党小李', avatar: '📚', content: '和同学毕业旅行，四个人订了两间双床房，性价比很高。' },
    { nickname: '城市探索者', avatar: '🏃', content: '白天暴走一天，晚上回来倒头就睡。床很舒服，体力恢复快。' },
    { nickname: '酒店控Linda', avatar: '🏨', content: '双床房的空间一点都不局促，两张床都是标准尺寸，不像有些酒店缩水。' },
    { nickname: '背包客阿明', avatar: '🎒', content: '拼房首选！和朋友AA下来价格太香了，房间品质也没打折。' },
    { nickname: '悠闲慢生活', avatar: '🌿', content: '商务房里算住过比较好的，灯光设计很用心，晚上看书不刺眼。' },
  ],
  5: [
    { nickname: '蜜月情侣', avatar: '💑', content: '豪华房太浪漫了！浴缸超级大，两个人泡都够。纪念日惊喜安排上了。' },
    { nickname: '酒店控Linda', avatar: '🏨', content: '豪华房的品质真的没话说，从床品到洗护用品都是高端品牌。值这个价。' },
    { nickname: '周末度假族', avatar: '🌴', content: '豪华体验绝对值回票价！从入住到退房全程享受，还会再来。' },
    { nickname: '商务张先生', avatar: '👔', content: '接待重要客户订的豪华房，面子十足。房间配置确实对得起价格。' },
    { nickname: '文艺青年M', avatar: '🎨', content: '豪华房的设计感太强了，每个角落都适合拍照。浴缸旁的落地窗绝了。' },
    { nickname: '旅行者小王', avatar: '🧳', content: '咬咬牙订了一晚豪华房犒劳自己，太值了！躺在浴缸里看夜景简直享受。' },
    { nickname: '悠闲慢生活', avatar: '🌿', content: '偶尔奢侈一下，豪华房的体验让人忘记所有烦恼。推荐给想犒劳自己的人。' },
    { nickname: '睡到自然醒', avatar: '😴', content: '床！太！舒！服！了！躺下去就不想起来，真的是云朵般的感觉。' },
    { nickname: '吃货一枚', avatar: '🍜', content: '豪华房的欢迎水果和点心很精致，mini吧也很丰富，吃货表示满意。' },
    { nickname: '摄影爱好者', avatar: '📷', content: '豪华房的落地窗采光绝了，拍了一组超级满意的照片。强烈推荐！' },
  ],
  6: [
    { nickname: '亲子家庭', avatar: '👨‍👩‍👧', content: '带娃出行首选！房间里有儿童玩具和用品，小朋友开心得不得了。' },
    { nickname: '旅行者小王', avatar: '🧳', content: '周末带孩子来住了一晚，亲子设施很齐全，连儿童拖鞋都准备了。' },
    { nickname: '周末度假族', avatar: '🌴', content: '一家三口来度周末，孩子有自己的床和小玩具，大人也能好好休息。' },
    { nickname: '吃货一枚', avatar: '🍜', content: '亲子房的儿童餐超可爱！小朋友吃得很开心，家长也能安心享用美食。' },
    { nickname: '悠闲慢生活', avatar: '🌿', content: '房间很大，孩子有足够的活动空间。隔音也好，不怕打扰到别人。' },
    { nickname: '酒店控Linda', avatar: '🏨', content: '亲子房的设计很用心，从安全防护到儿童用品都很到位。值得推荐！' },
    { nickname: '蜜月情侣', avatar: '💑', content: '虽然我们是情侣不是亲子出行，但是房间空间大住着也舒服。' },
    { nickname: '文艺青年M', avatar: '🎨', content: '亲子房的布置太可爱了，色彩搭配很温馨。连大人都被治愈到了。' },
    { nickname: '摄影爱好者', avatar: '📷', content: '亲子房的小帐篷简直太萌了，给孩子拍了超多可爱的照片。' },
    { nickname: '说走就走', avatar: '✈️', content: '临时带孩子出来玩，没想到这家酒店的亲子设施这么好。会再来的。' },
  ],
  7: [
    { nickname: '蜜月情侣', avatar: '💑', content: '套房太适合度蜜月了！独立的客厅和卧室，还有管家服务，满分体验。' },
    { nickname: '商务张先生', avatar: '👔', content: '套房用来商务接待非常有面子，客厅可以开会，卧室私密性也很好。' },
    { nickname: '酒店控Linda', avatar: '🏨', content: '这家酒店的套房绝对值五星好评！从硬件到服务都挑不出毛病。' },
    { nickname: '周末度假族', avatar: '🌴', content: '庆祝结婚纪念日订的套房，管家还帮忙布置了花瓣，太惊喜了！' },
    { nickname: '悠闲慢生活', avatar: '🌿', content: '偶尔奢侈一把，套房体验让人彻底放松。客厅看日落太美了。' },
    { nickname: '旅行者小王', avatar: '🧳', content: '用年终奖犒劳自己订了套房，一个字：值！两个字：太值了！' },
    { nickname: '文艺青年M', avatar: '🎨', content: '套房的艺术品味真的绝了，从家具到装饰画都很有格调。' },
    { nickname: '睡到自然醒', avatar: '😴', content: '套房的主卧床垫是顶级的，睡了一觉感觉整个人都重新充满了电。' },
    { nickname: '摄影爱好者', avatar: '📷', content: '套房的景观太棒了，落地窗外就是城市天际线，拍了好多延时摄影。' },
    { nickname: '吃货一枚', avatar: '🍜', content: '套房的行政酒廊待遇太好了，happy hour的小食和酒水都很精致。' },
  ],
}

/** 获取指定房型的虚拟评论（含模拟时间戳） */
export function getMockReviews(roomTypeId) {
  const list = mockReviews[roomTypeId] || []
  const now = Date.now()
  return list.map((item, i) => ({
    id: `mock-${roomTypeId}-${i}`,
    roomTypeId,
    nickname: item.nickname,
    avatar: item.avatar,
    content: item.content,
    createdAt: new Date(now - (list.length - i) * 86400000 * 3).toISOString(), // 每隔约 3 天一条
    _mock: true,
  }))
}

export default mockReviews
