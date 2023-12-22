tar -cf unity-scripts.tar -C ~/Projects unity-scripts
pscp -A -h ~/pssh_hosts unity-scripts.tar /Users/buildagent/Temp/unity-scripts.tar

pssh -h ~/pssh_hosts -A -i "tar -xf /Users/buildagent/Temp/unity-scripts.tar -C /Users/buildagent/Temp \
  && /Applications/Unity/Hub/Editor/2022.3.15f1/Unity.app/Contents/MacOS/Unity -quit -batchmode -projectPath /Users/buildagent/Temp/unity-scripts -executeMethod UnityConfig.UseEmbeddedAndroidTools -logFile temp.log && cat temp.log | grep '\[UnityConfig\]'"
