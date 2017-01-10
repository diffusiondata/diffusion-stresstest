# ulimit -n 8192

java -cp ./stresstest.jar:../clients/java/diffusion-client-*.jar com.pushtechnology.diffusion.stresstest.StressTest -Xms256m -Xmx512m

# > stdout.log 2>stderr.log &
