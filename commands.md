### List installed Android build tools
```
/Applications/Unity/Hub/Editor/2020.3.38f1/PlaybackEngines/AndroidPlayer/SDK/tools/bin/sdkmanager --list | grep -E 'build-tools;(\d|\.| )+\|(\d|\.| )+\|( |\w|-|\.)+\|'
build-tools;30.0.2   | 30.0.2  | Android SDK Build-Tools 30.0.2    | build-tools/30.0.2/
```

### Install Unity in multiple build agents
```
pssh -h ~/pssh_hosts -A -i "/Applications/Unity\ Hub.app/Contents/MacOS/Unity\ Hub -- --headless install --version 2022.3.16f1 -m android ios android-ndk android-sdk-build-tools android-sdk-ndk-tools android-sdk-platforms android-sdk-platform-tools --cm -a arm64"
```

### Accept Android SDK licenses
```
pssh -h ~/pssh_hosts -A -i "yes | /Applications/Unity/Hub/Editor/2020.3.38f1/PlaybackEngines/AndroidPlayer/SDK/tools/bin/sdkmanager --licenses"
```

### List installed Unity versions
```
pssh -h ~/pssh_hosts -A -i "/Applications/Unity\ Hub.app/Contents/MacOS/Unity\ Hub -- --headless editors -i"
```

### Run UnityConfig
```
/Applications/Unity/Hub/Editor/2022.3.16f1/Unity.app/Contents/MacOS/Unity -quit -batchmode -projectPath ~/Projects/sago-sample/Unity -executeMethod UnityConfig.UseEmbeddedAndroidTools -logFile temp.log && cat temp.log | grep '\[UnityConfig\]'
```

### Install Android build tools
```
pssh -h ~/pssh_hosts -A -i "/Users/buildagent/buildAgent/android/sdk/sdk-33/cmdline-tools/8.0/bin/sdkmanager \"build-tools;30.0.3\""
```