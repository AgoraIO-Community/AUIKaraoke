# Karaoke Theme

*[English](KaraokeTheme.md) | 中文*

AUIKit通过使用[SwiftTheme](https://github.com/wxxsw/SwiftTheme) 来作为一键换肤的基础能力

下面介绍如何为一个组件新增皮肤，以AScenesKit为例

### 1.创建皮肤目录
![](https://fullapp.oss-cn-beijing.aliyuncs.com/pic/1686831479688.jpg)

### 2.增加需要的自定义皮肤配置文件
```json
{
  "Test": {
      "backgroundImage": "aui_room_bg",           //图片资源
      "backgroundColor": "#00000000",             //颜色
      "iconWidth": 24,                            //宽度
      "iconHeight": 24,                           //高度
      "cornerRadius": 16,                         //圆角大小
      "normalFont": "PingFangSC-Semibold,17",     //字体及大小  
      "normalGradient": ["#099dfd", "#6c7192"]    //渐变色
  }
}
```

### 3.把新增的bundle路径设置进AUIKit里
```swift
if let folderPath = Bundle.main.path(forResource: "auiKaraokeTheme", ofType: "bundle") {
    AUIRoomContext.shared.addThemeFolderPath(path: URL(fileURLWithPath: folderPath) )
}
```

### 4. 对指定的View使用设置好的皮肤属性
```swift
let imageView = UIImageView()
//设置图片
imageView.theme_image = auiThemeImage("Test.backgroundImage")
//设置宽度
imageView.theme_width = "Test.iconWidth"
//设置高度
imageView.theme_height = "Test.iconHeight"
let label = UILabel()
//设置背景色
label.theme_backgroundColor = AUIColor("Test.backgroundColor")
//设置字体
label.theme_font = "Test.normalFont"

let gradientLayer = CAGradientLayer()
//设置渐变色
gradientLayer.theme_colors = AUIGradientColor("Test.normalGradient")
```

### 5.设置皮肤
#### 5.1 根据默认索引设置
设置索引index为0
```swift
AUIRoomContext.shared.resetTheme()
```
切换到下一个
```swift
AUIRoomContext.shared.switchThemeToNext()
```

#### 5.2 指定某个皮肤
```swift
AUIRoomContext.shared.switchTheme(themeName: "UIKit")
```

根据上述5步即可快速对一个新的组件进行皮肤切换