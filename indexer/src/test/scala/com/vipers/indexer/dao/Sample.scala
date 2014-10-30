package com.vipers.indexer.dao

import com.vipers.model.DatabaseModels._

trait Sample {
  object SampleCharacters {
    lazy val Xanot = Character("Xanot","xanot","5428035526967126513",45968,7576,26618375,100,0,1411,113707,0,114976,1,1359718034L,1412367959L,1412369810L,606,42025,1413670967359L)
    lazy val ThatTinkSound = Character("ThatTinkSound","thattinksound","5428232026854955473",607,63,265240,20,63,632,1266,0,670,1,1407074228,1407880533,1407881191,14,380,1413671172456L)
  }

  object SampleOutfitMemberships {
    lazy val Xanot = OutfitMembership(SampleOutfits.VIPR.id, SampleCharacters.Xanot.id, "Snuke", 2, System.currentTimeMillis())
  }

  object SampleOutfits {
    lazy val VIPR = Outfit("TheVipers", "thevipers", "VIPR", "vipr", "5428013610391601489", 126, 1, "37523756405021402", 1408310892L, 1413661746471L)
    lazy val CHI = Outfit("Chimera","chimera","CHI","chi","5428010917244925777",29,3,"37509528949362910",1353596129L,1413671746471L)
  }

  object SampleWeapons {
    lazy val NS15 = Weapon("75034","NS-15M",Some("Built on the concept of the NS-11A assault rifle, the remarkably accurate NS-15M LMG provides excellent sustained fire and above average projectile speed. All factions can use NS weapons."),None,"/files/ps2/images/static/8483.png",false,Some(800),Some(150),Some(150),Some(250),Some(300),1.0f,1.0f,None,None,None,1413671383847L,Some("22"))
    lazy val Corvus = Weapon("7152","Corvus VA55",Some("The Corvus VA55 features low recoil and high accuracy over sustained fire, making it a popular choice for long range engagements. VS use only."),Some(1),"/files/ps2/images/static/9470.png",false,Some(650),Some(150),Some(150),Some(250),Some(300),1.0f,1.0f,None,None,None,1413671383823L,Some("16"))
  }

  object SampleCharacterProfileStat {
    lazy val HeavyAssault = ProfileStat("5428035526967126513",6,2755,1087435,15285405)
    lazy val CombatMedic = ProfileStat("5428035526967126513",4,1237,1243963,8672186)
  }

  object SampleCharacterWeaponStat {
    lazy val NS15 = WeaponStat("5428035526967126513","75034",137910,50007,2639,5983,953,209402,2229007,1409415911L)
    lazy val Corvus = WeaponStat("5428035526967126513","7152",246806,86063,3344,10000,2420,646800,2158969,1391790581L)
  }
}
