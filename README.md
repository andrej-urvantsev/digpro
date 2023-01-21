![Application screenshow](./application.png)

## Build instructions

I used java 19, but java 17 should work as well

```bash
git clone https://github.com/lazystone/digpro.git
cd digpro
./gradlew clean build

# Run application
java -jar build/libs/digpro-0.0.1-SNAPSHOT.jar
```

## Notes

* You can use mouse to move over map(drag&drop it).
* There is not any tests but there isn't much business logic either.
* Method which is used to filter out visible locations is simple, for bigger datasets something like `kd-trees` should
  be used.
