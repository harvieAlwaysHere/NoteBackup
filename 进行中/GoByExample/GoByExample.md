## GoByExample

#### Start

* 直接运行，go run demo.go
* 编译并运行，go build demo.go，./demo

#### Base

* 值类型，string/int/float/bool

* 变量，声明和初始化，var name (type) = value

  * 可简写，name := value

* 常量，const name = value

* 循环，for (init;) conditon(; after) {}

  * 控制循环，break/return/continue

* 判断，if (init;) condition {}

* 多分支判断

  ```go
  switch i {
      case 1:
      case 2,3:
      default:
  }
  switch {
      case i == 1:
      case i == 2:
      default:
  }
  ```

#### Array/Slice

```go
// 数组
var a1 [5]int
a2 := [5]int{1, 2, 3, 4, 5}

// 切片
s1 := []string{"a"}
s1 = append(s1, "b", "c")

s2 := make([]string, len(s1))
copy(s2, s1)

s3 := s2[1:]
```

#### Map

```go
m := make(map[string]int)
m := map[string]int{"foo":1, "bar":2}

delete(m, key)
value, isExist := m[key]
```

#### Range

```go
// slice
for index, value := range values {}

// map
for key, value := range m {}

// string
for startByteIndex, codePoint := range "go" {}
```

#### Func

```go
func demoFunc(a string, b int) (string, int){
    return a, b
}

// 变参函数
func dynacParamsFunc(nums ...int){
    fmt.Print(nums. "")
}

// 闭包函数(累加功能) 初始化外部函数(extFunc)变量 返回内联函数(inFunc)
func extFunc(init int) func(add int) int {
    base := init
    return func(add int) int {
        base += add
        return base
    }
}

// 递归函数
func Fibonacci(n int) int {
    if n == 0 || n == 1 {
        return n
    }else {
        return Fibonacci(n - 1) + Fibonacci (n -2)
    }
}

func main(){
    a, b = demoFunc(3, "go")
    dynacParamsFunc(1, 2, 3)
    
    sliceParam := []int{1, 2, 3}
    dynacParamsFunc(sliceParam...)
    
    sumFunc := extFunc(0)
    fmt.Println(sumFunc(1))
    fmt.Println(sumFunc(2))
    fmt.Println(sumFunc(3))
}
```

#### Pointers

```go
// 值传递
func zeroval(val int){
    val = 0
}

// 指针传递
func zeroptr(valPtr *int){
    *valPtr = 0  // 解引用 从指针的内存地址获取对应值
}

func main(){
    i := 1
    zeroval(i)  // 不改变值
    zeroptr(&i)  // 改变值 &i获取i的内存地址 即指向i的指针
}
```

#### Struct

```go
type person struct {
    name string
    age int
}

// 结构体方法 指针传递 可修改结构体值
func (p *person) printPerson1() string {
    return "name is " + p.name + "age is" + p.age
}
// 结构体方法 值传递 生成结构体拷贝
func (p person) printPerson2() string {
    return "name is " + p.name + "age is" + p.age
}

// 接口
type behavior interface {
	getName() string
	getAge() int
}

// 结构体实现接口
func (p *person) getName() string {
	return p.name
}

func (p *person) getAge() int {
	return p.age
}

func main(){
    person := person{name : "Harvie", age : 25}
    fmt.Println(person.printPerson1())
    fmt.Println(person.printPerson2())
    
    personPtr := &person  // 指针调用 避免产生拷贝或修改结构体值
    fmt.Println(personPtr.printPerson1())
    fmt.Println(personPtr.printPerson2())
}
```

#### Error

```go
// 自定义Error结构体
type myError struct {
	code int
	message string
}

// 实现Error接口
func (m myError) Error() string {
	return "code is " + strconv.Itoa(m.code) + ", message is " + m.message
}

// 返回自定义Error
func getError(arg int) (int, error){
	if arg > 0 {
		return arg, nil
	}else {
		return -1, &myError{-1, "myError"}  // 构建自定义Error
	}
}
```

