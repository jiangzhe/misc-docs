---
layout: post
title: "Rust中的迭代器"
date: 2017-11-06 17:06:00 +0800
categories: Rust
permalink: /:categories/:title
---
最近对Rust很感兴趣，在这里写一点对Rust中迭代器的理解。

- Rust对迭代器的[定义](rustdoc-std-iter-trait)。

```rust
pub trait Iterator {
    type Item;
    fn next(&mut self) -> Option<Self::Item>;
    ...
}
```

核心方法是next()，其他的方法有默认实现。

可以和Java的Iterator接口比较一下。

```java
public interface Iterator<E> {
  boolean hasNext();
  E next();
  void remove();
}
```

Java增加了一个hasNext()方法来提供是否有后续元素的信息。
remove()方法放在这个接口里其实并不太合适。remove依赖底层数据结构的实现，有些可能不支持或很难在迭代时移除当前元素。

- Rust的Iterator特质提供了相当多的默认方法：

1. 取单个元素的方法：如first(), last(), nth()等。
2. 适配方法，如filter, take, skip等。
3. 高阶函数，如map, any, all, flat_map, fold, scan等。
4. 将Iterator组合的方法：chain, zip。

不来点函数式，怎敢说自己是现代编程语言？std::iter下也提供了三个很有用的构造方法empty, once, repeat。
在使用时，我们可以很流畅地写出声明式风格的代码。

- 实现Iterator

[文档](rustdoc-std-iter-impl)已经写得很清楚了。需要注意的是Rust中所有者权限的概念，iterator的参数类型究竟应该设置为T，&T，还是&mut T，是需要仔细考虑的。

- 与集合的互操作

尽管实现一个Iterator很简单，我们却很少会自己去实现，因为Rust标准库已经为常用的数据结构实现了与Iterator的互操作。设计与实现使用了Rust中的ATC（Associate type constructor），也许可以在下一篇文章里分析一下。

用法还是很便利的。

```rust
// iterator through &T
let arr1 = vec![1,2,3];
arr1.iter().for_each(|x| println!("{}", x));
// iterator through &mut T
let mut arr2 = vec![1,2,3];
arr2.iter_mut().for_each(|x| *x += 1 );
arr2.iter().for_each(|x| println!("{}", x));
// iterator through T
let arr3 = vec![1,2,3];
arr3.into_iter().for_each(|x| println!("{}", x));
// compile error: use of moved value
// arr3.iter().for_each(|x| println!("again {}", x));
```

简而言之，iter()提供了集合的只读迭代访问，iter_mut()允许对元素进行修改，into_iter()会消费掉原集合。
Rust提供了和Java比较类似的for语法糖，编译器会转译为into_iter()。

而从迭代器生成集合也十分方便。

```rust
// 1. from iterator through &T
let arr1 = vec![1,2,3];
let ll1: std::collections::LinkedList<i32> = arr1.iter().cloned().collect();
// 2. from iterator through T    
let arr2 = vec![1,2,3];
let ll2: std::collections::LinkedList<i32> = arr2.into_iter().collect();
```

可以注意一下cloned的使用是必要的，因为collect()并没有对&i32的实现。

- 一些例子

```rust
let squares: Vec<i32> = (0..).map(|x| x * x).take_while(|&x| x < 5000).collect();
let one_and_zero_infinity =
    std::iter::repeat(()).flat_map(|_| std::iter::once(1).chain(std::iter::once(0)));
let odds: Vec<i32> = squares
    .iter()
    .zip(squares.iter().skip(1))
    .map(|(&l, &r)| r - l)
    .collect();
```

[rustdoc-std-iter-trait]: [https://doc.rust-lang.org/std/iter/trait.Iterator.html]
[crate-rayon]: [https://crates.io/crates/rayon]
[rustdoc-std-iter-impl]: [https://doc.rust-lang.org/std/iter/index.html#implementing-iterator]