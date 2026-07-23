import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

const datePickerViews = [
  'src/modules/home/Home.vue',
  'src/views/user/roomtypes/RoomTypeListView.vue',
  'src/views/user/roomtypes/RoomTypeDetailView.vue'
]

describe('日期选择器插槽', () => {
  it.each(datePickerViews)('直接使用 Element Plus 提供的日期单元格对象', file => {
    const source = readFileSync(resolve(process.cwd(), file), 'utf8')
    expect(source).toContain('#default="cell"')
    expect(source).not.toContain('#default="{ cell }"')
  })
})
