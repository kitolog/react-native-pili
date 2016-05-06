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
    zoom={1}
    muted={true}
    />
```
###2. 直播播放
```javascript
<Player
    src={"rtmp://pili-live-rtmp.pilitest.qiniucdn.com/hubname/title"} //pili online url
    style={{
              height:400,
              width:400
            }}
    />
```
