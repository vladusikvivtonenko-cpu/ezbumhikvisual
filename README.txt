=== Як отримати jar (без встановлення нічого) ===
1) Створи репозиторій на github.com, залий туди ВМІСТ цієї папки (разом з прихованою .github).
2) Вкладка Actions -> build -> дочекайся зелёної галочки -> внизу Artifacts -> ezvisual-jar. Всередині ezvisual-1.0.0.jar.
3) Кинь jar в .minecraft/mods разом з Fabric Loader 0.16+ та Fabric API (1.21.4).

=== Або локально ===
JDK 21 + Gradle 8.12:  gradle build   ->  build/libs/ezvisual-1.0.0.jar

GUI: Right Shift (змінюється в Controls -> EzVisual).
Своя картинка в GUI: заміни src/main/resources/assets/ezvisual/textures/gui/anime.png
(PNG рівно 192x288 пікселів, малюється вдвічі меншою).
