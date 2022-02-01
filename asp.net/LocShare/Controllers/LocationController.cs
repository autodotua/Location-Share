using LocShare.Models;

using LocShare.Dto;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System;
using System.Collections.Generic;
using System.Linq;
using Mapster;

namespace LocShare.Controllers
{
    [Route("location")]
    public class LocationController : BaseController
    {
        [HttpGet]
        [Route("get")]
        public IActionResult GetAll(int? time)
        {
            using var db = new DbModel();
            var user = GetUser(db);
            if (user.GroupName == "test")
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

            DateTime someTimesBefore = DateTime.UtcNow.AddMinutes(-30);
            if (time.HasValue)
            {
                someTimesBefore = DateTime.UtcNow.AddSeconds(-time.Value);
            }

            var usersAndLocations = (from u in db.Users
                                     join l in db.Locations on u.LastLocationId equals l.Id into uls
                                     from ul in uls.DefaultIfEmpty()
                                     where u.GroupName == user.GroupName && u.Name != user.Name
                                     select new { u, ul }).ToArray();

            List<UserDto> users = new List<UserDto>();
            foreach (var item in usersAndLocations)
            {
                UserDto dto = user.Adapt<UserDto>();
                dto.LastLocation = item.ul.Adapt<LocationDto>();
                users.Add(dto);
            }

            return Ok(new Response<List<UserDto>>() { Data = users });
        }

        [HttpPost]
        [Route("update")]
        public IActionResult Update([FromBody] Request<LocationDto> request)
        {
            using var db = new DbModel();
            LocationDto location = request.Data;
            location.Time = DateTime.UtcNow;
            location.Username = UserName;
            var result = db.Locations.Add(location.Adapt<LocationEntity>());
            //db.Entry(location).State = EntityState.Added;

            db.SaveChanges();
            var user = GetUser(db);
            if (user != null)//理论上不会是null
            {
                user.LastUpdateTime = DateTime.UtcNow;
                user.LastLocationId = result.Entity.Id;
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
        public IActionResult Hide([FromBody] Request<object> request)
        {
            using var db = new DbModel();
            UserEntity user = GetUser(db);
            if (user != null)//理论上不会是null
            {
                user.LastUpdateTime = DateTime.UtcNow;
                user.LastLocationId = null;
                db.Entry(user).State = EntityState.Modified;

                db.SaveChanges();
            }

            return Ok(new Response<object>());
        }
    }
}