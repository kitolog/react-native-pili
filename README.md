#Pili Streaming Cloud React Native SDK
##Installation
Development

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
    onReady={()->{}} //onReady event
    onConnecting={()->{}} //onConnecting event
    onStreaming={()->{}} //onStreaming event
    onShutdown={()->{}} //onShutdown event
    onIOError={()->{}} //onIOError event
    onDisconnected={()->{}} //onDisconnected event
    />
```
###2. 直播播放
```javascript
<Player
  source={{
    uri:"rtmp://live.hkstv.hk.lxdns.com/live/hks",
    controller: true, //Controller ui
    timeout: 10 * 1000, //live streaming timeout (ms)
    live:true, //live streaming ?
    hardCodec:false, //hard codec [recommended false]
    }}
    style={{
      height:200,
      width:200,
    }}
    />
```
