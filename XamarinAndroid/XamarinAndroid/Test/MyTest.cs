using System;
using System.Collections.Generic;

namespace XamarinAndroid.Test
{
    public class MyTest
    {
        private IDictionary<Type,int> dictionary;
        public MyTest()
        {
            dictionary = new Dictionary<Type, int>()
            {
                {typeof(Employee), 30 },
                {typeof(Employee), 31 }
            };
            
        }
        public void printDictionay()
        {
            foreach(KeyValuePair<Type,int> keyValuePair in dictionary)
            {
                Console.WriteLine("Key: {0}, Value: {1}", keyValuePair.Key, keyValuePair.Value);
            }
        }
    }

    class Employee
    {
        private string _name;
        private int _age;
        public string Name
        {
            get { return _name; }
            set { _name = value; }
        }

        public int Age
        {
            get { return _age; }
            set { _age = value; }
        }

    }
}
