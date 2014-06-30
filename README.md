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

Dále je možno přidat následující možnosti (nejsou povinné). Zde je uvedena jejich výchozí hodnota.

```
# Uložit hodnocení z ČSFD
csfd.rating=True 

# Země: all = ukládat všechny země / first = jen první zemi 
csfd.countries=all  

# Stahovat obaly
csfd.poster=False    

# Přepsat herce z IMDb
csfd.actors=False  

# Přepsat režiséry
csfd.directors=False 

# Přepsat autory scénáře
csfd.writers=False     
```
