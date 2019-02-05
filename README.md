# サポートOS
- Android 7.0以上

# サポート端末
- ARCore動作サポート端末
https://developers.google.com/ar/discover/supported-devices#android_play

# 使用ライブラリ
[ARCore](https://developers.google.com/ar/develop/)

[ViroCore](https://virocore.viromedia.com/docs/overview)

# ビルド方法
## 開発環境
- Android Studio 3.1以上

## 手順
1. [ViroCore](https://s3-us-west-2.amazonaws.com/virocore/1_12_0/virocore-release-v_1_12_0.aar)をダウンロードし、viro_coreディレクトリ配下へダウンロードしたaarを配置します。

2. [Viromediaサイト](https://viromedia.com/signup) からAPIキーを取得し、appモジュール配下のAndroidManifest.xmlに記述します。
```
<meta-data android:name="com.viromedia.API_KEY" android:value="＜APIキーを記述してください＞" />
```
