using System;
using LocShare.Controllers;
using LocShare.Models.Entity;
using LocShare.Models.Transmission;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace LocShare.Test
{
    [TestClass]
    public class Test
    {
        [TestMethod]
        public void TestMethod1()
        {
            HomeController controller = new HomeController();
            GetOption option = new GetOption() { Time = (int)(TimeSpan.FromDays(300).TotalSeconds) };
            UserEntity user = new UserEntity() { Name = "test", GroupName = "" };
            controller.GetAll(new Request<GetOption>() { Data = option, User = user });
        }
    }
}
