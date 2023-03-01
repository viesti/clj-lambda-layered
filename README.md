# JVM/Clojure Lambda with deps in a layer

## Rationale

AWS Lambda supports layers, so with a AWS provided JVM Runtime, we can put [library dependencies in a layer](https://docs.aws.amazon.com/lambda/latest/dg/configuration-layers.html#configuration-layers-path).

The code in this repository shows how to split a JVM/Clojure project into

* Lambda with only the handler class AOT compiled
* Layer with the application dependencies

The AWS provided JVM Runtime looks up a handler class, with specific method, so we need to provide such a class.

The trick is to use [requiring-resolve](https://clojuredocs.org/clojure.core/requiring-resolve) in the Lambda handler, to postpone compilation of application code other than the handler, to AOT only the handler class.

## Further investigation

Postponing AOT to first event invocation would make the first invocation slow, but with [AWS Lambda Snapstart](https://aws.amazon.com/blogs/aws/new-accelerate-your-lambda-functions-with-lambda-snapstart/), we could put the compilation to happen at the Snapstart invocation phase.

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