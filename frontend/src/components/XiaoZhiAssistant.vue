<template>
  <div
    class="assistant-anchor"
    :style="{ left: `${position.left}px`, top: `${position.top}px` }"
  >
    <button
      v-if="!open"
      class="assistant-ball"
      type="button"
      title="打开智宿酒店小助手"
      @pointerdown.stop="startBallDrag"
    >
      <span class="ball-label">小智</span>
    </button>

    <section v-else class="assistant-panel">
      <header class="panel-header" @pointerdown.stop="startPanelDrag">
        <div class="header-main">
          <span class="header-avatar">智</span>
          <div>
            <strong>小智</strong>
            <small>智宿酒店小助手</small>
          </div>
        </div>
        <button class="icon-btn" type="button" @click="closePanel">收起</button>
      </header>

      <div ref="messageListRef" class="message-list">
        <article
          v-for="message in messages"
          :key="message.id"
          class="message-item"
          :class="message.role"
        >
          <div class="message-bubble">
            <p>{{ message.content }}</p>
          </div>
        </article>

        <article v-if="loading" class="message-item assistant">
          <div class="message-bubble typing">
            <span></span>
            <span></span>
            <span></span>
          </div>
        </article>
      </div>

      <div class="prompt-list">
        <button
          v-for="prompt in quickPrompts"
          :key="prompt"
          type="button"
          class="prompt-chip"
          @click="sendMessage(prompt)"
        >
          {{ prompt }}
        </button>
      </div>

      <footer class="panel-footer">
        <textarea
          v-model.trim="draft"
          class="composer"
          rows="3"
          maxlength="500"
          placeholder="可以询问预订入住流程、酒店功能、周边景点或三亚天气。"
          @keydown.enter.exact.prevent="sendMessage()"
        />
        <div class="footer-actions">
          <span class="hint-text">Shift + Enter 可换行</span>
          <button class="send-btn" type="button" :disabled="loading || !draft" @click="sendMessage()">
            发送
          </button>
        </div>
      </footer>
    </section>
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { chatWithAssistant } from '@/api/assistant'

const BALL_SIZE = 68
const PANEL_WIDTH = 368
const PANEL_HEIGHT = 560
const SAFE_GAP = 12
const STORAGE_KEY = 'zhisu_xiaozhi_position'

const open = ref(false)
const loading = ref(false)
const draft = ref('')
const messageListRef = ref(null)
const position = ref(getInitialPosition())
const dragState = ref(null)
const quickPrompts = [
  '我该如何预订房间？',
  '前台怎样办理入住和退房？',
  '附近有什么值得去的景点？',
  '今天三亚天气怎么样？'
]
const messages = ref([
  {
    id: createId(),
    role: 'assistant',
    content: '您好，我是智宿酒店小助手小智。您可以向我咨询酒店预订、入住退房、系统使用方法、周边景点，也可以询问三亚天气。'
  }
])

function createId() {
  return `${Date.now()}_${Math.random().toString(16).slice(2)}`
}

function getInitialPosition() {
  if (typeof window === 'undefined') {
    return { left: 24, top: 96 }
  }

  try {
    const cached = JSON.parse(window.localStorage.getItem(STORAGE_KEY) || 'null')
    if (cached && Number.isFinite(cached.left) && Number.isFinite(cached.top)) {
      return cached
    }
  } catch {
    // ignore broken cache
  }

  return {
    left: window.innerWidth - BALL_SIZE - 24,
    top: 92
  }
}

function normalizePosition(left, top, panelVisible = open.value) {
  const minLeft = panelVisible ? PANEL_WIDTH - BALL_SIZE + SAFE_GAP : SAFE_GAP
  const maxLeft = Math.max(SAFE_GAP, window.innerWidth - BALL_SIZE - SAFE_GAP)
  const maxTop = Math.max(SAFE_GAP, window.innerHeight - (panelVisible ? PANEL_HEIGHT : BALL_SIZE) - SAFE_GAP)

  return {
    left: Math.min(Math.max(left, minLeft), maxLeft),
    top: Math.min(Math.max(top, SAFE_GAP), maxTop)
  }
}

function persistPosition() {
  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(position.value))
}

function updatePosition(nextLeft, nextTop, panelVisible = open.value) {
  position.value = normalizePosition(nextLeft, nextTop, panelVisible)
  persistPosition()
}

function togglePanel() {
  open.value = !open.value
  position.value = normalizePosition(position.value.left, position.value.top, open.value)
  persistPosition()
  if (open.value) {
    scrollToBottom()
  }
}

function closePanel() {
  open.value = false
  position.value = normalizePosition(position.value.left, position.value.top, false)
  persistPosition()
}

function startDrag(event, mode) {
  dragState.value = {
    mode,
    pointerId: event.pointerId,
    startX: event.clientX,
    startY: event.clientY,
    startLeft: position.value.left,
    startTop: position.value.top,
    moved: false
  }

  window.addEventListener('pointermove', handlePointerMove)
  window.addEventListener('pointerup', handlePointerUp)
}

function startBallDrag(event) {
  startDrag(event, 'ball')
}

function startPanelDrag(event) {
  startDrag(event, 'panel')
}

function handlePointerMove(event) {
  if (!dragState.value || dragState.value.pointerId !== event.pointerId) {
    return
  }

  const deltaX = event.clientX - dragState.value.startX
  const deltaY = event.clientY - dragState.value.startY
  const moved = Math.abs(deltaX) > 4 || Math.abs(deltaY) > 4

  dragState.value.moved ||= moved

  updatePosition(
    dragState.value.startLeft + deltaX,
    dragState.value.startTop + deltaY,
    dragState.value.mode === 'panel'
  )
}

function handlePointerUp(event) {
  if (!dragState.value || dragState.value.pointerId !== event.pointerId) {
    return
  }

  const current = dragState.value
  dragState.value = null
  window.removeEventListener('pointermove', handlePointerMove)
  window.removeEventListener('pointerup', handlePointerUp)

  if (current.mode === 'ball' && !current.moved) {
    togglePanel()
  } else {
    position.value = normalizePosition(position.value.left, position.value.top, current.mode === 'panel')
    persistPosition()
  }
}

function scrollToBottom() {
  nextTick(() => {
    const container = messageListRef.value
    if (container) {
      container.scrollTop = container.scrollHeight
    }
  })
}

async function sendMessage(content = draft.value) {
  const text = String(content || '').trim()
  if (!text || loading.value) {
    return
  }

  draft.value = ''
  if (!open.value) {
    open.value = true
    position.value = normalizePosition(position.value.left, position.value.top, true)
  }

  messages.value.push({
    id: createId(),
    role: 'user',
    content: text
  })
  scrollToBottom()

  loading.value = true
  try {
    const history = messages.value
      .slice(-12)
      .map(message => ({ role: message.role, content: message.content }))

    const result = await chatWithAssistant({
      message: text,
      history
    })

    const answer = result?.data?.answer || result?.message || '小智暂时没有生成回复，请稍后重试。'
    messages.value.push({
      id: createId(),
      role: 'assistant',
      content: answer
    })
  } catch (error) {
    messages.value.push({
      id: createId(),
      role: 'assistant',
      content: error?.response?.data?.message || '当前无法连接到小智服务，请稍后再试。'
    })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

function handleResize() {
  position.value = normalizePosition(position.value.left, position.value.top, open.value)
  persistPosition()
}

onMounted(() => {
  position.value = normalizePosition(position.value.left, position.value.top, false)
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  window.removeEventListener('pointermove', handlePointerMove)
  window.removeEventListener('pointerup', handlePointerUp)
})
</script>

<style scoped>
.assistant-anchor {
  position: fixed;
  z-index: 2100;
}

.assistant-ball {
  width: 68px;
  height: 68px;
  border: 0;
  border-radius: 999px;
  background: linear-gradient(135deg, #496454 0%, #a4774f 100%);
  box-shadow: 0 18px 38px rgba(50, 42, 32, 0.24);
  color: #fff;
  cursor: grab;
  display: flex;
  align-items: center;
  justify-content: center;
}

.assistant-ball:active {
  cursor: grabbing;
}

.ball-label {
  font-size: 17px;
  font-weight: 700;
  letter-spacing: 0.06em;
}

.assistant-panel {
  position: absolute;
  top: 0;
  right: 0;
  width: 368px;
  height: min(560px, calc(100vh - 24px));
  min-height: 420px;
  display: flex;
  flex-direction: column;
  min-height: 0;
  transform: translateX(calc(-100% + 68px));
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 24px;
  overflow: hidden;
  background: rgba(255, 252, 247, 0.96);
  backdrop-filter: blur(18px);
  box-shadow: 0 24px 68px rgba(48, 39, 30, 0.18);
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 18px;
  background: linear-gradient(135deg, #33483b 0%, #496454 55%, #a4774f 100%);
  color: #fff;
  cursor: grab;
  user-select: none;
}

.panel-header:active {
  cursor: grabbing;
}

.header-main {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-avatar {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  background: rgba(255, 255, 255, 0.16);
  font-size: 19px;
  font-weight: 700;
}

.header-main strong,
.header-main small {
  display: block;
}

.header-main strong {
  font-size: 16px;
}

.header-main small {
  margin-top: 4px;
  color: rgba(255, 255, 255, 0.8);
}

.icon-btn {
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 999px;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
  cursor: pointer;
}

.message-list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 18px;
  padding-right: 10px;
  scrollbar-width: thin;
  scrollbar-color: rgba(164, 119, 79, 0.8) rgba(232, 223, 209, 0.75);
  background:
    radial-gradient(circle at top right, rgba(164, 119, 79, 0.08), transparent 28%),
    linear-gradient(180deg, rgba(255, 253, 248, 0.95), rgba(248, 243, 234, 0.95));
}

.message-list::-webkit-scrollbar {
  width: 8px;
}

.message-list::-webkit-scrollbar-track {
  background: rgba(232, 223, 209, 0.75);
  border-radius: 999px;
}

.message-list::-webkit-scrollbar-thumb {
  background: rgba(164, 119, 79, 0.85);
  border-radius: 999px;
}

.message-list::-webkit-scrollbar-thumb:hover {
  background: rgba(143, 98, 63, 0.95);
}

.message-item {
  display: flex;
  margin-bottom: 14px;
}

.message-item.user {
  justify-content: flex-end;
}

.message-bubble {
  max-width: 86%;
  padding: 12px 14px;
  border-radius: 18px;
  background: #fff;
  color: #3d342d;
  line-height: 1.7;
  box-shadow: 0 10px 24px rgba(55, 45, 35, 0.07);
}

.message-item.user .message-bubble {
  background: #496454;
  color: #fff;
  border-bottom-right-radius: 6px;
}

.message-item.assistant .message-bubble {
  border-bottom-left-radius: 6px;
}

.message-bubble p {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
}

.typing {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.typing span {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: #b99a6b;
  animation: typing-bounce 1s infinite ease-in-out;
}

.typing span:nth-child(2) {
  animation-delay: 0.15s;
}

.typing span:nth-child(3) {
  animation-delay: 0.3s;
}

.prompt-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 12px 16px 0;
}

.prompt-chip {
  border: 1px solid #e5d9ca;
  border-radius: 999px;
  padding: 8px 12px;
  background: #fff;
  color: #5e554d;
  cursor: pointer;
  font-size: 12px;
}

.prompt-chip:hover {
  border-color: #a4774f;
  color: #8f623f;
}

.panel-footer {
  padding: 12px 16px 16px;
}

.composer {
  width: 100%;
  resize: none;
  padding: 12px 14px;
  border: 1px solid #e1d6c8;
  border-radius: 16px;
  background: #fff;
  color: #3d342d;
  line-height: 1.6;
  outline: none;
}

.composer:focus {
  border-color: #a4774f;
}

.footer-actions {
  margin-top: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.hint-text {
  color: #988a7a;
  font-size: 12px;
}

.send-btn {
  border: 0;
  border-radius: 999px;
  padding: 10px 18px;
  background: #33483b;
  color: #fff;
  cursor: pointer;
}

.send-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

@keyframes typing-bounce {
  0%,
  80%,
  100% {
    transform: translateY(0);
    opacity: 0.5;
  }
  40% {
    transform: translateY(-4px);
    opacity: 1;
  }
}

@media (max-width: 768px) {
  .assistant-panel {
    width: min(92vw, 360px);
  }
}
</style>
