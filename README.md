ČSFD.cz YAMJ Plugin
===================
__Written by__: Ike Blaster

Created for use by Yet Another Movie Jukebox 2.10 r3763 ([YAMJ](http://code.google.com/p/moviejukebox/)), but anyone can use it however he wants.

[ČSFD](http://csfd.cz) is the largest czech and slovak movie database. This plugin uses unofficial [ČSFD API](http://csfdapi.cz) to retrieve data from the database.

Project is maintained purely in IntelliJ IDEA IDE.

Following notes are only in czech, considering the plugin targets on czech/slovak users.


Použití
-------
Plugin je připraven pro použití v balíčku **Y2M By Peter Butler V2.0.5 Mod Shizzl Yamj 2.10 r3763 12October2013**, případně v YAMJ verze 2.10 r3763. Jiné verze nemusí být podporované, struktura tříd se v jednotlivých verzích mění. (Pro nezávislost na jednotlivé verzi je nutné do programu přidělat rozhraní pro další plugin.)

Stáhnout nebo získat další informace o balíčku můžete [zde](http://www.mede8erforum.com/index.php/topic,12503.0.html).

Jakmile máte balíček rozbalený, je nutné do složky <code>lib</code> zkopírovat soubor  <code>[csfdplugin.jar](https://github.com/ikeblaster/csfdcz_yamj_plugin/raw/master/out/artifacts/csfdplugin_jar/csfdplugin.jar)</code>.

Dále je třeba vybrat v nastavení tento plugin pro získávání informací o filmech. To lze buď z grafického rozhraní, nebo editací souboru <code>moviejukebox-Mede8er.properties</code>. Do něj je třeba přidat (nebo upravit, pokud tam zde již tato položka je):
```
mjb.internet.plugin=info.thez.csfdplugin.CSFDplugin
```


Konfigurace pluginu
-------------------
Plugin umožňuje změnu některých parametrů funkčnosti. V tomto seznamu je uvedena výchozí hodnota. **Pokud chcete toto nastavení zpřístupnit, je nutné celý následující text zkopírovat na konec souboru <code>properties\moviejukebox-default.properties</code>.** 

```
# Ulozit hodnoceni z CSFD
csfd.rating=True
# Prepsat herce z IMDb
csfd.actors=True
# Prepsat rezisery
csfd.directors=True
# Prepsat autory scenare
csfd.writers=True
# Stahovat obaly
csfd.poster=False
# Stahovat fanarty z CSFD
csfd.fanart=False
# Zeme: all = ukladat vsechny zeme / first = jen prvni zemi
csfd.countries=all
```

