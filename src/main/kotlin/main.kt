import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.toObservable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject
import java.io.File
import kotlin.text.Charsets.UTF_8

sealed class Droid : Throwable() {
    class OU812 : Droid()
}

sealed class FileReadError : Throwable() {
    class FileNotFound : FileReadError()
}

fun main(args: Array<String>) {

//    exampleOf("creating observable") {
//        val mostPopular: Observable<String> = Observable.just(episodeV)
//        val originalTrilogy = Observable.just(episodeIV, episodeV, episodeVI)
//        val prequelTrilogy = Observable.just(listOf(episodeI, episodeII, episodeII))
//        val sequelTrilogy = Observable.fromIterable(listOf(episodeII, episodeIII, episodeIX))
//
//        val stories = listOf(solo, rogueOne).toObservable()
//    }

    exampleOf("subscribe") {

        val observable = Observable.just(episodeIV, episodeV, episodeVI)
//        observable.subscribe { element ->
//            println(element)
//        }

        observable.subscribeBy(
                onNext = {
                    println(it)
                },
                onComplete = {
                    println("Completed")
                }
        )
    }

    exampleOf("empty") {
        val observable = Observable.empty<Unit>()

        observable.subscribeBy(
                onNext = {
                    println(it)
                },
                onComplete = {
                    println("Completed")
                }
        )
    }

    exampleOf("never") {
        val subscriptions = CompositeDisposable()

        Observable.never<Any>()
                .doOnNext {
                    println("doOnNext")
                }
                .doOnSubscribe {
                    println("On subscribe")
                }
                .doOnDispose {
                    println("Disposed")
                }
                .subscribeBy(
                        onNext = {
                            println(it)
                        },
                        onComplete = {
                            println("Completed")
                        }
                ).addTo(subscriptions)

        subscriptions.clear()


    }

    exampleOf("dispose") {
        val mostPopular: Observable<String> = Observable.just(episodeV, episodeIV, episodeVI)

        val subscription = mostPopular.subscribe {
            println(it)
        }

        subscription.dispose()
    }

    exampleOf("CompositeDisposable") {

        val subscriptions = CompositeDisposable()
        subscriptions.add(listOf(episodeVII, episodeI, rogueOne)
                .toObservable()
                .subscribe {
                    println(it)
                }
        )

    }

    /*  End of Subscribing to Observables, Part 1
     *
     *
     */

    exampleOf("create") {
        val subscriptions = CompositeDisposable()

        val droids = Observable.create<String> { emitter ->
            emitter.onNext("R2-D2")
            emitter.onError(Droid.OU812())
            emitter.onNext("C-3PO")
            emitter.onNext("K-2S0")
        }

        val observer = droids.subscribeBy(
                onNext = { println(it) },
                onError = { println("Error, $it") },
                onComplete = { println("Completed") }
        )

        subscriptions.add(observer)
    }

    exampleOf("Single") {
        val subscriptions = CompositeDisposable()

        fun loadText(fileName: String): Single<String> {
            return Single.create { emitter ->
                val file = File(fileName)

                if (!file.exists()) {
                    emitter.onError(FileReadError.FileNotFound())
                    return@create
                }

                val contents = file.readText(UTF_8)
                emitter.onSuccess(contents)
            }
        }

        val observer = loadText("ANewHope.txt")
                .subscribe(
                        {
                            println(it)
                        },
                        {
                            println("Error $it")
                        }
                )

        subscriptions.add(observer)

    }

    /*  End of Subscribing to Observables, Part 2
   *
   *
   */

    exampleOf("PublishSubject") {

        val quotes = PublishSubject.create<String>()
        quotes.onNext(itsNotMyFault)

        val subscriptionOne = quotes.subscribeBy(
                onNext = { printWithLabel("1)", it) },
                onComplete = { printWithLabel("1", "Complete") }
        )

        quotes.onNext(doOrDoNot)

        val subscriptionTwo = quotes.subscribeBy(
                onNext = { printWithLabel("2)", it) },
                onComplete = { printWithLabel("2", "Complete") }
        )

        quotes.onNext(lackOfFaith)

        subscriptionOne.dispose()

        quotes.onNext(eyesCanDeceive)

        quotes.onComplete()

        val subscriptionThree = quotes.subscribeBy(
                onNext = { printWithLabel("3)", it) },
                onComplete = { printWithLabel("3", "Complete") }
        )

        quotes.onNext(stayOnTarget)

        subscriptionTwo.dispose()
        subscriptionThree.dispose()

    }

    exampleOf("BehaviourSubject") {

        val subscriptions = CompositeDisposable()

        val quotes = BehaviorSubject.createDefault(iAmYourFather)

        val subscriptionOne = quotes.subscribeBy(
                onNext = { printWithLabel("1)", it) },
                onError = { printWithLabel("1)", it) },
                onComplete = { printWithLabel("1", "Complete") }
        )

        quotes.onError(Quote.NeverSaidThat())

        quotes.subscribeBy(
                onNext = { printWithLabel("1)", it) },
                onError = { printWithLabel("1)", it) },
                onComplete = { printWithLabel("1", "Complete") }
        ).addTo(subscriptions)

    }

    exampleOf("BehaviourSubject state") {

        val subscriptions = CompositeDisposable()

        val quotes = BehaviorSubject.createDefault(mayTheForceBeWithYou)
        println(quotes.value)

        quotes.subscribeBy(
                onNext = { printWithLabel("1)", it) },
                onError = { printWithLabel("1)", it) },
                onComplete = { printWithLabel("1", "Complete") }
        ).addTo(subscriptions)

        quotes.onNext(mayThe4thBeWithYou)
        println(quotes.value)
    }

    exampleOf("ReplaySubject") {

        val subscriptions = CompositeDisposable()

        val quotes = ReplaySubject.createWithSize<String>(2)
        quotes.onNext(useTheForce)

        quotes.subscribeBy(
                onNext = { printWithLabel("1)", it) },
                onError = { printWithLabel("1)", it) },
                onComplete = { printWithLabel("1", "Complete") }
        ).addTo(subscriptions)

        quotes.onNext(theForceIsStrong)
        quotes.onNext(mayThe4thBeWithYou)

        quotes.subscribeBy(
                onNext = { printWithLabel("2)", it) },
                onError = { printWithLabel("2)", it) },
                onComplete = { printWithLabel("2", "Complete") }
        ).addTo(subscriptions)

        subscriptions.dispose()

    }

    /*  End of Subjects
      *
      *
     */


    exampleOf("PublishSubject") {

        val subscriptions = CompositeDisposable()

        val dealtHand = PublishSubject.create<List<Pair<String, Int>>>()

        fun deal(cardCount: Int) {
            val deck = cards
            var cardsRemaining = 52
            val hand = mutableListOf<Pair<String, Int>>()

            (0 until cardCount).forEach {
                val randomIndex = (0 until cardsRemaining).random()
                hand.add(deck[randomIndex])
                deck.removeAt(randomIndex)
                cardsRemaining -= 1

            }

            if (points(hand) > 21) {
                dealtHand.onError(HandError.Busted())
            } else {
                dealtHand.onNext(hand)
            }

            // Add code to update dealtHand here

        }

        dealtHand.subscribeBy(
                onNext = { printWithLabel("1)", it) },
                onError = { printWithLabel("1)", it) },
                onComplete = { printWithLabel("1", "Complete") }
        ).addTo(subscriptions)

        // Add subscription to dealtHand here

        deal(3)
    }

}

