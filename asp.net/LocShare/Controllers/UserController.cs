using LocShare.Models;

using LocShare.Dto;
using LocShare.Service;
using Microsoft.AspNetCore.Mvc;
using Mapster;
using Microsoft.AspNetCore.Http;
using System.Diagnostics;
using Microsoft.EntityFrameworkCore;
using System.Collections.Generic;
using System.Linq;

namespace LocShare.Controllers
{
    [Route("user")]
    public class UserController : BaseController
    {
        /// <summary>
        /// 注册
        /// </summary>
        /// <param name="request"></param>
        /// <returns></returns>
        [HttpPost]
        [Route("signUp")]
        public IActionResult SignUp([FromBody] Request<UserDto> request)
        {
            using var db = new DbModel();
            if (db.Users.Find(request.Data.Name) != null)
            {
                return Ok(new Response<string>() { Succeed = false, Message = "用户名已存在" });
            }
            if (request.Data.Password.Length != 32)
            {
                return Ok(new Response<string>() { Succeed = false, Message = "密码不合法" });
            }
            request.Data.GroupName = "";
            var user = new UserEntity()
            {
                Name = request.Data.Name,
                DisplayName = request.Data.DisplayName,
                Password = request.Data.Password,
            };
            db.Users.Add(user);
            db.SaveChanges();

            return Ok(new Response<string>() { Data = TokenFilter.GetToken(user) });
        }

        /// <summary>
        /// 登录
        /// </summary>
        /// <param name="request"></param>
        /// <returns></returns>
        [HttpPost]
        [Route("signIn")]
        public IActionResult SignIn([FromBody] Request<UserDto> request)
        {
            using var db = new DbModel();
            var user = db.Users.Find(request.Data.Name);
            if (user == null)
            {
                return Ok(new Response<UserEntity>() { Succeed = false, Message = "用户不存在" });
            }
            if (user.Password != request.Data.Password)
            {
                return Ok(new Response<UserEntity>() { Succeed = false, Message = "用户名或密码错误" });
            }
            var dto = user.Adapt<UserDto>();
            dto.Token = TokenFilter.GetToken(user);
            dto.Password = null;
            return Ok(new Response<UserDto>() { Data = dto });
        }

        [HttpPost]
        [Route("userInfo")]
        public IActionResult SetUserInfo([FromBody] Request<UserDto> request)
        {
            using var db = new DbModel();
            UserEntity user = db.Users.Find(UserName);
            UserDto newUser = request.Data;
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

        [HttpGet]
        [Route("members")]
        public IActionResult GetGroupMenbers()
        {
            using var db = new DbModel();
            var user = GetUser(db);
            if (user.GroupName == null)
            {
                return Ok(new Response<List<UserEntity>>() { Data = new List<UserEntity>() });
            }

            var users = (from p in db.Users where p.GroupName == user.GroupName && p.Name != user.Name select p).ToList();
            foreach (var u in users)
            {
                u.Password = null;
                u.GroupName = null;
            }
            return Ok(new Response<List<UserEntity>>() { Data = users });
        }
    }
}