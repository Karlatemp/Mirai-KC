# Mirai-KC

## 准备构建

运行 `gradlew jar` 获取IDEA编译用依赖库

运行 `gradlew shadowjar` 获取完整构建, 但是此jar无法用于jar依赖


### 运行

#### 在 IDEA 运行

首先我们准备一个scope
```kotlin
val scope = CoroutineScope(CoroutineThreadPool)
scope.subscribeAlways<MessageEvent> {
}
```
是的，和Mirai-demo基本一致

当我们的工作都准备完成后, 执行下面的这一句
```kotlin
io.github.karlatemp.miraikc.bootstrap.Main.main(arrayOf())
```
现在我们的入口看起来是这样的
```kotlin
fun main() {
    val scope = CoroutineScope(CoroutineThreadPool)
    scope.subscribeAlways<MessageEvent> {
    }

    io.github.karlatemp.miraikc.bootstrap.Main.main(arrayOf())
}
```

如果出现了错误, 你还可以在 `fun main(){}` 开头加上一句
`System.setProeprty("mirai.idea", "")`

#### 在插件模式运行


首先编写一个插件, 见 `MoRain`

创建一个文件夹, 叫做 `plugins`

我们将插件构建放在这个文件夹里面. 然后打开我们的终端

- For Windows
```shell script
java -cp "mirai-android-XXX.jar;Mirai-KC-XXXX.shadow.jar" io.github.karlatemp.miraikc.bootstrap.Main
```

- For Linux
```shell script
java -cp mirai-android-XXX.jar:Mirai-KC-XXXX.shadow.jar io.github.karlatemp.miraikc.bootstrap.Main
```


