#Pili Streaming Cloud React Native SDK
##Installation
```
Run npm install react-native-video --save
```
###iOS
1. In XCode, in the project navigator, right click Libraries ➜ Add Files to [your project's name]
2. Go to node_modules ➜ react-native-pili ➜ RCTPili and add RCTPili.xcodeproj
3. In XCode, in the project navigator, select your project. Add libPTCPili.a ,libz.tbd,libc++.tbd to your project's Build Phases ➜ Link Binary With Libraries
4. Run your project (Cmd+R)

###Android

**android/settings.gradle**
```
include ':react-native-pili'
project(':react-native-pili').projectDir = new File(settingsDir, '../node_modules/react-native-pili/android')
```
**android/app/build.gradl**
```
dependencies {
    ...
    compile project(':react-native-pili')
}
```
**MainActivity.java**

On top, where imports are:
```java
import com.pili.rnpili.PiliPackage;
```

Modify getPackages method
```java
 @Override
    protected List<ReactPackage> getPackages() {
        return Arrays.<ReactPackage>asList(
                new MainReactPackage(),
                new PiliPackage(this)
        );
    }
```


##TODO
- [x] Android Player 70%
- [x] Android Streaming 90%
- [x] iOS Player 70%
- [ ] iOS Streaming 

##Usage
###1. 推流
```javascript
<Streaming
    stream={{ 
        id:"xxx", //pili id
        title:"title", //pili title
        hub:"hubname", //pili hub name
        publishKey:"<PK>", //pili key
        publishSecurity:"static", //pili secrity policy (static or dynamic)
        hosts:{
          publish:{ //pili Streaming url (support rtmp)
            rtmp:"pili-publish.pilitest.qiniucdn.com"
          }
        }
        }}
    style={{
        height:400,
        width:400,
    }}
    zoom={1} //zoom 
    muted={true} //muted
    focus={false} //focus
    profile={{  //video and audio profile
       video:{
         fps:30,
         bps:1000 * 1024,
         maxFrameInterval:48
       },
       audio:{
         rate:44100,
         bitrate:96 * 1024
       },
    started={false} //streaming status
    onReady={()=>{}} //onReady event
    onConnecting={()=>{}} //onConnecting event
    onStreaming={()=>{}} //onStreaming event
    onShutdown={()=>{}} //onShutdown event
    onIOError={()=>{}} //onIOError event
    onDisconnected={()=>{}} //onDisconnected event
    />
```
###2. 直播播放
```javascript
<Player
  source={{
    uri:"rtmp://live.hkstv.hk.lxdns.com/live/hks",
    controller: true, //Controller ui  Android only
    timeout: 10 * 1000, //live streaming timeout (ms) Android only
    live:true, //live streaming ? Android only
    hardCodec:false, //hard codec [recommended false]  Android only
    }}
    started={true} //iOS only
    muted={false} //iOS only
    style={{
      height:200,
      width:200,
    }}
    onLoading={()=>{}} //loading from remote or local
    onPaused={()=>{}} //pause event
    onShutdown={()=>{}} //stopped event
    onError={()=>{}} //error event
    onPlaying={()=>{}} //play event
    />
```
