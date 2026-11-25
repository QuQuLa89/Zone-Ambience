# ZoneAmbience

[![CI](https://github.com/QuQuLa89/Zone-Ambience/actions/workflows/ci.yml/badge.svg)](https://github.com/QuQuLa89/Zone-Ambience/actions/workflows/ci.yml)

Kotlin製の[PaperMC](https://papermc.io/)サーバー向け、指定範囲に入ったプレイヤー個人にだけパーティクル・疑似天候・個人時間・環境音を表示するプラグインです。

ワールド全体の時間や天候を変更したり、建築の景観を損ねるブロックを設置したりすることなく、建築エリアやイベント会場ごとに独自の空気感を演出できます。

## 主な機能

- 個人向けの空間演出: パーティクル、疑似天候、個人時間（朝・昼・夕暮れ・夜）、環境音をエリア内にいるプレイヤーのクライアントだけに反映します。
- 砂漠でも降る疑似天候: パーティクルで雨・雪・嵐を降らせるため、本来雨が降らないバイオームでも雪や雨を降らせられます。
- ワールド天候からの独立: エリア内に入っている間は個人のバニラ天気を晴れ（`CLEAR`）に固定するため、ワールドが雨や雷雨のときでもエリア独自の演出だけを保てます。
- ワンドと非設置アイテムの両対応: 専用のブレイズロッドワンドで四角（Box）または円柱（Cylinder）の範囲を指定し、コマンド（`/zamb create`）または染料などの非設置アイテムを右クリックして空間へ適用できます。
- 可視化と直感的なGUI: ワンド所持中または `/zamb visualize` が有効な間は境界線と中心の光点を表示し、中心を `Shift + 右クリック` するか `/zamb list` から設定・項目一覧・アイテム回収メニューを開けます。
- 安全対策とアイテム回収: クラフト・金床・砥石・鍛冶台・かまど燃料・羊や看板の染色などへの悪用を防ぎ、GUI からはインベントリの空きを確認したうえでエリアを撤去して元のアイテムへ戻せます。
- WorldGuard 連携: 範囲内のサンプル点すべてで建築権限とカスタムフラグ `zone-ambience`（既定 `allow`）を検証します。
- 独自の操作履歴とロールバック: エリアの作成・変更・削除・回収・復元を `history.yml` に記録し、`/zamb history` と `/zamb rollback` で取り消せます。
- 個人表示トグル: プレイヤーごとに `/zamb toggle` で表示と環境音の ON/OFF を切り替えられます。

## 動作環境

- Paper（または Paper ベースのフォーク） 1.21.4 以降
- Java 21
- 任意連携: WorldGuard 7.0.13 以降

## インストール方法

1. ビルド済みの `ZoneAmbience-<version>.jar` を用意します。[ビルド方法](#ビルド方法) を参照するか、Releases ページから取得してください。
2. JAR ファイルをサーバーの `plugins/` フォルダーに配置します。
3. サーバーを再起動してください。

## 使い方

### 専用アイテムを使う場合

1. `/zamb wand` で「パーティクル範囲指定ワンド」を取得します。
2. ワンドで範囲を指定します（左クリックで Pos1／円柱の中心、右クリックで Pos2／円柱の半径と高さ、Shift + 右クリックで四角と円柱を切り替え）。
3. 範囲を指定した状態で、`/zamb give` などで入手したパーティクルアイテムを持って右クリックすると、ブロックを置かずに空間へエリアを適用します。
4. 可視化中にエリア中心の白いパーティクルを `Shift + 右クリック` するか、`/zamb list` を実行すると、パーティクル・疑似天候・個人時間・環境音・密度・範囲更新・アイテム回収を GUI から操作できます。

## コマンド

すべてのコマンドは `/zoneambience`（エイリアス: `/zamb`, `/za`）配下にあります。

| コマンド | 説明 | 権限 |
|---|---|---|
| `/zamb help` | コマンド一覧を表示します。 | `zoneambience.use` |
| `/zamb toggle` | 自分への表示・個人時間・環境音の ON/OFF を切り替えます。 | `zoneambience.use` |
| `/zamb wand` | 範囲指定ワンド（特殊ブレイズロッド）を取得します。 | `zoneambience.wand` |
| `/zamb shape <box\|cylinder>` | 選択形状を四角または円柱に切り替えます。 | `zoneambience.wand` |
| `/zamb radius <数値>` | 円柱モードの半径を指定します。 | `zoneambience.wand` |
| `/zamb height <minY> <maxY>` | 円柱モードの高さを指定します。 | `zoneambience.wand` |
| `/zamb visualize` | エリア境界線の常時可視化 ON/OFF を切り替えます。 | `zoneambience.wand` |
| `/zamb list [all]` | エリア一覧・設定・回収 GUI を開きます。 | `zoneambience.wand` |
| `/zamb edit <名前>` | 指定エリアの設定 GUI を開きます。 | `zoneambience.wand` |
| `/zamb remove <名前>` | 指定エリアを削除します（アイテムは戻りません）。 | `zoneambience.wand` |
| `/zamb create <名前>` | 選択範囲に新規エリアを作成し、設定 GUI を開きます。 | `zoneambience.create` |
| `/zamb give <プレイヤー> <プリセット> [個数]` | プリセット設定入りのパーティクルアイテムを配布します。 | `zoneambience.give` |
| `/zamb history [エリア名\|件数]` | 操作履歴を表示します。 | `zoneambience.history` |
| `/zamb rollback <履歴ID>` | 指定した履歴の変更前の状態へ復元します。 | `zoneambience.rollback` |

### 配布プリセット例

`/zamb give` では `cherry`、`desert_rain`、`desert_snow`、`storm`、`night_stars`、`sunset_firefly`、`crimson`、`warped`、`ash` のプリセットキーを指定できます。

## 権限

| 権限 | デフォルト | 説明 |
|---|---|---|
| `zoneambience.use` | true | `/zamb toggle` による個人表示の ON/OFF 切り替えを行えます。 |
| `zoneambience.wand` | true | ワンド使用・アイテム適用・自エリアの編集・回収・削除を行えます。 |
| `zoneambience.create` | op | コマンド `/zamb create` で新規エリアを作成できます。 |
| `zoneambience.give` | op | `/zamb give` でパーティクルアイテムを配布できます。 |
| `zoneambience.history` | op | `/zamb history` で操作履歴を閲覧できます。 |
| `zoneambience.rollback` | op | `/zamb rollback` で変更前の状態へ復元できます。 |
| `zoneambience.admin` | op | 全エリアの閲覧・編集・回収・削除・サイズや所持数上限および WorldGuard 制限のバイパスを行えます。 |

## 保存データ

プラグインデータフォルダー（`plugins/ZoneAmbience/`）には、`config.yml`（上限・描画間隔・履歴保持件数）、`areas.yml`（登録済みエリア情報）、`history.yml`（操作履歴スナップショット、既定最大 500 件）、`preferences.yml`（プレイヤーごとの個人表示 OFF 設定）を保存します。

## ビルド方法

Windows では次を実行します。

```powershell
.\gradlew.bat clean build
```

Linux または macOS では次を実行します。

```bash
./gradlew clean build
```

テストとビルドが成功すると、シェード済み JAR を `build/libs/ZoneAmbience-<version>.jar` へ出力します。

## ライセンス

[MIT License](LICENSE) のもとで公開しています。
