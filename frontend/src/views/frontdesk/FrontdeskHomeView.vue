<template>
  <section class="frontdesk-home">
    <div class="welcome">
      <div><span>今日接待</span><h2>欢迎进入前台工作台</h2><p>这里与游客网站、住客中心和管理员后台相互独立，专门承载入住、房态与退房业务。</p></div>
      <div class="date-card"><small>TODAY</small><strong>{{ today }}</strong></div>
    </div>

    <el-alert v-if="true" title="前台业务接口尚未接入" type="warning" :closable="false" show-icon>
      <template #default>当前已完成前台角色校验和独立工作区；入住、分房、退房、清洁等按钮将在成员4接口完成后启用，页面不会伪造业务数据。</template>
    </el-alert>

    <div class="operation-grid">
      <article v-for="item in operations" :key="item.title">
        <span class="op-index">{{ item.index }}</span>
        <div><h3>{{ item.title }}</h3><p>{{ item.description }}</p><small>等待业务接口接入</small></div>
        <button disabled>暂未开放</button>
      </article>
    </div>

    <div class="guide-panel">
      <div><span>ROLE BOUNDARY</span><h3>各身份页面现在如何区分？</h3></div>
      <ul><li><b>游客</b>浏览首页和房型，可注册或登录。</li><li><b>住客</b>增加个人中心与信用中心。</li><li><b>前台</b>进入本接待工作区，不显示系统配置菜单。</li><li><b>管理员</b>进入运营后台，管理内容、价格、库存和系统。</li></ul>
    </div>
  </section>
</template>

<script setup>
const today = new Intl.DateTimeFormat('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'short' }).format(new Date())
const operations = [
  { index: '01', title: '入住办理', description: '核对订单、登记住客并确认入住' },
  { index: '02', title: '分房与房态', description: '分配实体房间并查看实时房态' },
  { index: '03', title: '退房办理', description: '完成退房确认与房态流转' },
  { index: '04', title: '清洁管理', description: '跟踪待清洁、清洁中与可售状态' }
]
</script>

<style scoped>
.frontdesk-home{display:flex;flex-direction:column;gap:22px}.welcome{display:flex;align-items:center;justify-content:space-between;padding:34px 38px;border-radius:18px;background:linear-gradient(125deg,#fffdf8 0%,#e8eee9 100%);box-shadow:0 14px 40px rgba(48,62,51,.08)}.welcome span,.guide-panel span{color:#9b7652;font-size:10px;letter-spacing:.2em}.welcome h2{margin:8px 0 10px;font-family:"Noto Serif SC",serif;font-size:30px}.welcome p{margin:0;color:#786d62;line-height:1.7}.date-card{min-width:190px;padding:18px;border-left:1px solid #d7d0c4}.date-card small{display:block;color:#9b7652;letter-spacing:.2em}.date-card strong{display:block;margin-top:8px;color:#395548;font-size:15px}.operation-grid{display:grid;grid-template-columns:repeat(2,1fr);gap:16px}.operation-grid article{display:grid;grid-template-columns:auto 1fr auto;gap:16px;align-items:center;padding:25px;border:1px solid #e4ddd1;border-radius:15px;background:#fffdf9}.op-index{font-family:Georgia,serif;font-size:30px;color:#c4ab87}.operation-grid h3{margin:0 0 7px;font-size:17px}.operation-grid p{margin:0 0 9px;color:#83776c;font-size:13px}.operation-grid small{color:#b28762}.operation-grid button{padding:9px 12px;border:0;border-radius:8px;background:#eee9e0;color:#aaa097}.guide-panel{display:grid;grid-template-columns:.8fr 1.2fr;gap:40px;padding:30px 34px;border-radius:16px;background:#334b40;color:#fff}.guide-panel h3{margin:8px 0 0;font-family:"Noto Serif SC",serif;font-size:22px}.guide-panel ul{margin:0;padding-left:20px;color:rgba(255,255,255,.72);line-height:2}.guide-panel b{color:#fff}
@media(max-width:900px){.operation-grid{grid-template-columns:1fr}.welcome{align-items:flex-start;gap:20px}.date-card{display:none}.guide-panel{grid-template-columns:1fr}}
</style>
