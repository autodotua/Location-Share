using LocShare.Models;
using LocShare.Models.Entity;
using LocShare.Models.Transmission;
using LocShare.Service;
using Microsoft.AspNetCore.Cors;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Net;
using System.Threading.Tasks;

namespace LocShare.Controllers
{
    public class HomeController : ControllerBase
    {
        private DbModel db = new DbModel();

        [HttpGet]
        [Route("index")]
        public string Index(int? a)
        {
            return "程序正在运行";
        }

        private Dictionary<string, string> userIdToName = new Dictionary<string, string>();

        [HttpPost]
        [Route("members")]
        public IActionResult GetGroupMenbers(Request<object> request)
        {
            if (!TokenService.IsTokenValid(db, request.User))
            {
                return Unauthorized();
            }
            if (request.User.GroupName == null)
            {
                return Ok(new Response<List<UserEntity>>() { Data = new List<UserEntity>() });
            }

            var users = (from p in db.User where p.GroupName == request.User.GroupName && p.Name != request.User.Name select p).ToList();
            foreach (var user in users)
            {
                user.Password = null;
                user.GroupName = null;
            }
            return Ok(new Response<List<UserEntity>>() { Data = users });
        }

        [HttpPost]
        [Route("get")]
        public IActionResult GetAll(Request<GetOption> request)
        {
            if (!TokenService.IsTokenValid(db, request.User))
            {
                return Unauthorized();
            }

            if (request.User.GroupName == "test")
            {
                List<LocationEntity> locations = new List<LocationEntity>();
                Random r = new Random();
                foreach (var i in Enumerable.Range(1, 20))
                {
                    locations.Add(new LocationEntity()
                    {
                        Username = "test" + i,
                        Time = DateTime.Now.AddMinutes(-r.NextDouble() * 10),
                        Latitude = 10 + 20 * r.NextDouble(),
                        Longitude = 110 + 20 * r.NextDouble(),
                        Accuracy = 50 * r.NextDouble(),
                        Speed = 50 * r.NextDouble(),
                        Altitude = 500 * r.NextDouble(),
                        Id = i
                    });
                }
                return Ok(new Response<List<LocationEntity>>() { Data = locations });
            }

            GetOption option = request.Data;
            DateTime someTimesBefore = DateTime.UtcNow.AddMinutes(-30);
            if (option != null)
            {
                someTimesBefore = DateTime.UtcNow.AddSeconds(-option.Time);
            }

            //var usersAndLocations = (from u in db.User
            //                         join l in db.Location on u.Name equals l.Username into uls
            //                         from ul in uls.DefaultIfEmpty()
            //                         where ul==default || u.GroupName == request.User.GroupName && ul.Time > someTimesBefore && u.Name != request.User.Name
            //                         select new { User = u, Location = ul });
            //var b = usersAndLocations.Select(p => p.User.Name).Distinct().ToArray();
            //HashSet<UserEntity> userWithLocations = new HashSet<UserEntity>();

            //Dictionary<string, dynamic> userNameToUserLocation = new Dictionary<string, dynamic>();

            //foreach (var item in usersAndLocations)
            //{
            //    if(item.Location==null)
            //    {
            //        //如果左连接无右值，则直接加入集合
            //        userWithLocations.Add(item.User);
            //        continue;
            //    }

            //   if(userNameToUserLocation.ContainsKey(item.User.Name))
            //    {
            //        //如果之前已经有其它位置记录了，比较时间，保留时间较晚的
            //        if(item.Location.Time> userNameToUserLocation[item.User.Name].Location.Time)
            //        {
            //            userNameToUserLocation[item.User.Name] = item;
            //        }
            //    }
            //   else
            //    {
            //        //否则直接加入
            //        userNameToUserLocation.Add(item.User.Name, item);
            //    }
            //}

            //foreach (var item in userNameToUserLocation.Values)
            //{
            //    ((UserEntity)item.User).LastLocation = item.Location;
            //    userWithLocations.Add(item.User);
            //}
            var usersAndLocations = (from u in db.User
                                     join l in db.Location on u.LastLocationId equals l.Id into uls
                                     from ul in uls.DefaultIfEmpty()
                                     where u.GroupName == request.User.GroupName && u.Name != request.User.Name
                                     select new { u, ul }).ToArray();

            foreach (var item in usersAndLocations)
            {
                item.u.LastLocation = item.ul;
                item.u.Password = null;
                item.u.GroupName = null;
            }
            var users = usersAndLocations.Select(p => p.u).ToList();

            return Ok(new Response<List<UserEntity>>() { Data = users });
        }

        [HttpPost]
        [Route("update")]
        public IActionResult Update(Request<LocationEntity> request)
        {
            if (!TokenService.IsTokenValid(db, request.User))
            {
                return Unauthorized();
            }
            LocationEntity location = request.Data;
            location.Time = DateTime.UtcNow;
            location.Username = request.User.Name;
            db.Location.Add(location);
            //db.Entry(location).State = EntityState.Added;

            db.SaveChanges();
            UserEntity user = db.User.Find(request.User.Name);
            if (user != null)//理论上不会是null
            {
                user.LastUpdateTime = DateTime.UtcNow;
                user.LastLocationId = location.Id;
                db.Entry(user).State = EntityState.Modified;

                db.SaveChanges();
            }

            return Ok(new Response<object>());
        }

        /// <summary>
        /// 注册
        /// </summary>
        /// <param name="request"></param>
        /// <returns></returns>
        [HttpPost]
        [Route("hide")]
        public IActionResult Hide(Request<object> request)
        {
            if (!TokenService.IsTokenValid(db, request.User))
            {
                return Unauthorized();
            }
            UserEntity user = db.User.Find(request.User.Name);
            if (user != null)//理论上不会是null
            {
                user.LastUpdateTime = DateTime.UtcNow;
                user.LastLocationId = null;
                db.Entry(user).State = EntityState.Modified;

                db.SaveChanges();
            }

            return Ok(new Response<object>());
        }

        /// <summary>
        /// 注册
        /// </summary>
        /// <param name="request"></param>
        /// <returns></returns>
        [HttpPost]
        [Route("signUp")]
        public IActionResult SignUp(Request<UserEntity> request)
        {
            if (db.User.Find(request.Data.Name) != null)
            {
                return Ok(new Response<string>() { Succeed = false, Message = "用户名已存在" });
            }
            if (request.Data.Password.Length != 32)
            {
                return Ok(new Response<string>() { Succeed = false, Message = "密码MD5长度不合法" });
            }
            request.Data.GroupName = "";
            db.User.Add(request.Data);
            db.SaveChanges();

            return Ok(new Response<string>() { Data = TokenService.GetToken(request.Data) });
        }

        /// <summary>
        /// 登录
        /// </summary>
        /// <param name="request"></param>
        /// <returns></returns>
        [HttpPost]
        [Route("signIn")]
        public IActionResult SignIn(Request<UserEntity> request)
        {
            var user = db.User.Find(request.Data.Name);
            if (user == null)
            {
                return Ok(new Response<UserEntity>() { Succeed = false, Message = "用户不存在" });
            }
            if (user.Password != request.Data.Password)
            {
                return Ok(new Response<UserEntity>() { Succeed = false, Message = "用户名或密码错误" });
            }
            user.Token = TokenService.GetToken(request.Data);
            user.Password = null;
            return Ok(new Response<UserEntity>() { Data = user });
        }

        /// <summary>
        /// 检查Token
        /// </summary>
        /// <param name="request"></param>
        /// <returns></returns>
        [HttpPost]
        [Route("checkToken")]
        public IActionResult CheckToken(Request<object> request)
        {
            var ok = TokenService.IsTokenValid(db, request.User, out string message);
            return Ok(new Response<object>() { Succeed = ok, Message = message });
        }

        [HttpPost]
        [Route("userInfo")]
        public IActionResult SetUserInfo(Request<UserEntity> request)
        {
            if (!TokenService.IsTokenValid(db, request.User))
            {
                return Unauthorized();
            }
            UserEntity user = db.User.Find(request.User.Name);
            UserEntity newUser = request.Data;
            Debug.Assert(newUser != null);
            if (user != null)
            {
                if (newUser.GroupName != null)
                {
                    user.GroupName = newUser.GroupName;
                }
                if (!string.IsNullOrEmpty(newUser.Password))
                {
                    user.Password = newUser.Password;
                }
                if (newUser.DisplayName != null)
                {
                    user.DisplayName = newUser.DisplayName;
                }
                db.Entry(user).State = EntityState.Modified;
                db.SaveChanges();
                return Ok(new Response<object>());
            }
            return NotFound();
        }
    }
}