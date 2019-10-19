
package java.lang.reflect;

/**
 * 每个代理实例都有一个关联的调用处理程序。 当在代理实例上调用方法时，方法调用将被编码并分派到其调用处理程序的invoke方法。
 * 实现InvocationHandler接口可以实现JDK的动态代理
 */
public interface InvocationHandler {


    /**
     * @param proxy  ---代理实例,真实代理对象com.sun.proxy.$Proxy0
     * @param method ---需要被代理的方法实例,我们所要调用某个对象真实的方法的Method对象
     * @param args   ---被代理方法的参数，数组形式，有多少就传多少
     * @return
     * @throws Throwable
     */
    //处理代理实例上的方法调用并返回结果。
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable;
}
    /*
     public class AddServiceHandler implements InvocationHandler {
      //代理类中的真实对象
     private AddService addService;

    //构造函数，给我们的真实对象赋值
     public AddServiceHandler(AddService addService) {
     this.addService = addService;
     }

     public AddService getProxy() {
     return (AddService) Proxy.newProxyInstance(addService.getClass().getClassLoader(),
     addService.getClass().getInterfaces(), this);
     }

     @Override
     public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
     System.out.println("before");
     Object invoke = method.invoke(addService, args);
     System.out.println("after");
     return invoke;
     }
     }

     =====================================客户端
     public static void main(String[] args) {
            //要代理的真实对象
            People people = new Teacher();
            //代理对象的调用处理程序，我们将要代理的真实对象传入代理对象的调用处理的构造函数中，最终代理对象的调用处理程序会调用真实对象的方法
            InvocationHandler handler = new WorkHandler(people);
            /**
             * 通过Proxy类的newProxyInstance方法创建代理对象，我们来看下方法中的参数
             * 第一个参数：people.getClass().getClassLoader()，使用handler对象的classloader对象来加载我们的代理对象
             * 第二个参数：people.getClass().getInterfaces()，这里为代理类提供的接口是真实对象实现的接口，这样代理对象就能像真实对象一样调用接口中的所有方法
             * 第三个参数：handler，我们将代理对象关联到上面的InvocationHandler对象上
 People proxy = (People)Proxy.newProxyInstance(handler.getClass().getClassLoader(), people.getClass().getInterfaces(), handler);
        //System.out.println(proxy.toString());
        System.out.println(proxy.work());
     */
