# Poke.dex

So one day, I saw someone post their [Jetpack Compose pokedex app](https://github.com/skydoves/pokedex-compose) on /r/androiddev on Reddit as supposedly following the current "best practices" around Android app development. I took it as a challenge to build the same thing, but **ignoring** all the things that Google recommends on this particular season and/or that I might be required to like if I cared about the labor market.

<img src="/images/screenshots.jpg"/>

## Download

Go to [releases](https://github.com/grishka/poke.dex/releases/latest) to download the latest apk.

## Tech stack & open-source libraries
- Minimum SDK level is 21 (Android 5.0).
- Java 17 based, utilizing [Runnable](https://developer.android.com/reference/java/lang/Runnable)s posted to [HandlerThread](https://developer.android.com/reference/android/os/HandlerThread)s and back to the main thread for asynchronous operations.
- Jetpack libraries:
  - None really.
  - There are RecyclerView and SwipeRefreshLayout from [LiteX](https://github.com/grishka/LiteX), my de-appcompat-ified fork of several AndroidX libraries.
  - AndroidX and Jetpack aren't the same thing, are they? Or is AndroidX part of Jetpack?
- Architecture:
  - Something MVC-ish I guess? I don't really like following patterns for the sake of following patterns.
  - I do sometimes end up with a pattern in my code by pure coincidence because that ends up being the most optimal way of solving the task at hand.
- [OkHttp3](https://github.com/square/okhttp/tree/okhttp_3.14.x) for networking.
- [Appkit](https://github.com/grishka/appkit) for some boilerplate and an image loader.
- [Gson](https://github.com/google/gson) for JSON parsing.
- [Parceler](https://github.com/johncarl81/parceler) for one (1) class that I needed to be parcelable.
  - Yes it's overkill in this particular case. But then I used Parceler in another project and I liked it (I used an Android Studio plugin to generate parcelables before that). And I want to show off my Android stack to contrast it with the "best practice" one anyway.
- Android's built-in SQLite for caching.
  - No fancy abstraction libraries on top of it. It's `Cursor`s all the way down!

## Open API
Poke.dex uses the [PokeAPI](https://pokeapi.co/) to retrieve the Pokémon data.

## FAQ

#### Is this project serious?
Absolutely. This is more-or-less how I've been doing Android apps for around 10 years, including ones used by millions of people.

#### Have you tried { technology.name }?
Probably not. I'm not very open to innovation in programming because most of the current "innovations" involve piling ever more abstraction layers on top of each other for no other benefit than to say "my code looks beautiful" or "this project can be worked on even by most junior of developers who need not understand the abstraction layers beneath the topmost one".

I do like where Java is going though, hence Java 17.
