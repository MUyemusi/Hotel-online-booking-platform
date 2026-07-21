<template>
  <router-view />
</template>

<script setup>
</script>

<style>
#app { min-height: 100vh; font-family: Inter, "PingFang SC", "Microsoft YaHei", sans-serif; -webkit-font-smoothing: antialiased; -moz-osx-font-smoothing: grayscale; }
* { box-sizing: border-box; }
html { scroll-behavior: smooth; }
html, body { margin: 0; min-width: 320px; background: #f7f3ea; color: #302a24; }
button, input, textarea, select { font: inherit; }
:root {
  --el-color-primary: #496454;
  --el-color-primary-light-3: #6f8678;
  --el-color-primary-light-5: #91a196;
  --el-color-primary-light-7: #bcc7c0;
  --el-color-primary-light-9: #eef2ef;
  --el-color-primary-dark-2: #33483b;
  --el-border-radius-base: 8px;
  --el-bg-color-page: #f7f3ea;
  --el-text-color-primary: #302a24;
  --el-text-color-regular: #665d54;
}
::selection { background: #b99a6b; color: #fff; }
</style>
