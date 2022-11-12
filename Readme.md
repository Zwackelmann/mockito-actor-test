Simple project to showcase unexpected behavior of [mockito-scala](https://github.com/mockito/mockito-scala)  `withObjectMocked` context function when combining it with akka `Actors`.

In `Main.scala` there are two test setups `simpleSetup` and `actorSetup` that should both invoke the __mocked__ `FooService.doit()` method.

When using the `actorSetup` however, the real implementation is invoked despite being in the `withObjectMocked` context.

To reproduce, switch setup by commenting in/out the respective setup in `Main.scala` from:

```
val result: Try[String] = simpleSetup()
// val result: Try[String] = actorSetup()
```

to

```
// val result: Try[String] = simpleSetup()
val result: Try[String] = actorSetup()
```

Using the `simpleSetup`, the code finishes with printing `finished with result: mock result`
Using the `actorSetup`, the code finishes with printing `failed with exception: executed real impl!!!`

Since both are executed within the `withObjectMocked` context, expected behavior is that both finish with `finished with result: mock result`
