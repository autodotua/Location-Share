namespace LocShare.Models.Entity
{
    using System;
    using System.Data.Entity;
    using System.ComponentModel.DataAnnotations.Schema;
    using System.Linq;

    public partial class DbModel : DbContext
    {
        public DbModel()
            : base("name=DbModel")
        {
        } public DbModel(string connStr)
            : base(connStr)
        {
        }

        public virtual DbSet<UserEntity> User { get; set; }
        public virtual DbSet<LocationEntity> Location { get; set; }

        protected override void OnModelCreating(DbModelBuilder modelBuilder)
        {
            modelBuilder.Entity<UserEntity>()
                .Property(e => e.Password)
                .IsFixedLength()
                .IsUnicode(false);

            //modelBuilder.Entity<LocationEntity>()
            //    .Property(e => e.UserId)
            //    .IsFixedLength()
            //    .IsUnicode(false);
        }
    }
}
