# 健身记录（GymLog）

一个**纯本地**的安卓健身记录 App，记录推 / 拉 / 腿三套计划的每次训练。

- 技术栈：Kotlin + Jetpack Compose + Room + MVVM
- 包名：`com.chaoge.gymlog`
- 最低系统：Android 8.0（API 26）
- 数据全部存本机（Room 数据库），无网络、无账号、无云同步

## 功能

- **今日**：逐组记录重量 / 次数 / 完成；动作切换（原动作保留）；跳过；备注；组间休息倒计时（90 秒，可 +30 / 跳过）；完成训练
- **计划**：推 / 拉 / 腿 三套计划查看（内置默认计划，首次启动自动生成）
- **历史**：月历视图，点有训练的天看当天记录
- **动作历史**：点记录里的动作名，看这个动作「上次练了多少」
- **自动存草稿**：练到一半退出，已勾的组下次打开还在

## 怎么跑起来

> 这台电脑目前没有装 Android 开发环境，需要先装 **Android Studio**（自带 JDK 和 SDK）。

1. 下载安装 Android Studio（官网 developer.android.com/studio，装最新稳定版即可）。
2. 首次启动它会引导下载 Android SDK（一路默认下一步）。
3. 打开 Android Studio → **File → Open** → 选中本项目文件夹 `gymlog`。
4. 等右下角 Gradle 同步完成（第一次会联网下载依赖，需要几分钟）。
5. 顶部选一个设备：
   - **用模拟器**：菜单 Tools → Device Manager → 创建一个设备 → 点 ▶ 运行；
   - **用真机**：手机打开「开发者选项 → USB 调试」，插上电脑，选你的手机点 ▶ 运行。

## 首次使用

打开 App 后，会自动内置一套默认计划（推日 / 拉日 / 腿日各 5 个动作 + 备选）。今日页默认显示「推日」，点「推日 ▼」可切到拉日 / 腿日。

## 目录结构

```
gymlog/
├── app/src/main/java/com/chaoge/gymlog/
│   ├── MainActivity.kt            # 入口 + 底部导航 + 页面路由
│   ├── GymLogApplication.kt       # 持有数据库和仓库
│   ├── data/
│   │   ├── entity/Entities.kt     # Room 表结构（6 张表）
│   │   ├── dao/Daos.kt            # 数据访问
│   │   ├── AppDatabase.kt
│   │   ├── Seed.kt                # 内置默认计划
│   │   └── Repository.kt          # 业务逻辑
│   ├── domain/Models.kt           # 领域模型 + 常量
│   └── ui/                        # 各页面 + ViewModel
└── app/src/main/res/              # 图标、主题
```

## 数据表（Room）

- `exercise` 动作库（原计划 / 备选 / 自建动作）
- `plan_exercise` 计划动作模板
- `alternative` 备选动作关联
- `workout_session` 训练记录（每天一条）
- `workout_exercise` 当天动作记录
- `set_record` 每组记录

## 说明

- 「切换动作」会**清空该动作已勾的组**（因为是不同动作），原动作仍在候选列表里可切回。
- 计划编辑（整条增删动作、新建整套计划、每动作单独休息时间）留到第二版。
- 未做：重量建议、系统动作库、数据导出、磅/公斤切换。
