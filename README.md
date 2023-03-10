# JVM/Clojure Lambda with deps in a layer

## Rationale

AWS Lambda supports layers, so with a AWS provided JVM Runtime, we can put [library dependencies in a layer](https://docs.aws.amazon.com/lambda/latest/dg/configuration-layers.html#configuration-layers-path).

The code in this repository shows how to split a JVM/Clojure project into

* Lambda with only the handler namespace AOT compiled
* Layer with the library dependencies

The AWS provided JVM Runtime looks up a handler class, with a specific event handler method, so we need to provide such a class.

The trick is to use [requiring-resolve](https://clojuredocs.org/clojure.core/requiring-resolve) in the Lambda handler, to compile only the handler and not all application code, and to [AOT only the handler namespace](https://github.com/viesti/clj-lambda-layered/blob/181c54488f5c39aee9674432b41238fbccc67e60/build.clj#L19).

Postponing Clojure code to bytecode compilation to happen during the first event handler invocation would make the first invocation slow, but with [AWS Lambda Snapstart](https://aws.amazon.com/blogs/aws/new-accelerate-your-lambda-functions-with-lambda-snapstart/), we can put the compilation to happen at the Snapstart invocation phase.

The code in this example makes the clojure compiler run at checkpoint creation time via [runtime hook](https://docs.aws.amazon.com/lambda/latest/dg/snapstart-runtime-hooks.html):

```clojure
(defn -beforeCheckpoint [this context]
  (println "Before checkpoint")
  ;; Do stuff here that would result in compiling the clojure application code, so the resulting process state can be checkpointed via Firecracker VM
  ((requiring-resolve 'layer-demo.core/get-clojure))
  (println "Before checkpoint done"))
```

### Usage

Compile the lamdba via

```shell
clj -T:build lambda
```

This produces `target/lambda.jar`, which you can upload to a Lambda with JVM runtime.

Compile the layer via

```shell
clj -T:build layer
```

This produces `target-layer/layer.zip`, which you can upload as a layer. Select the layer for the Lambda to use.
