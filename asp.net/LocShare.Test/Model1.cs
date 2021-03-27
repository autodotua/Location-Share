namespace LocShare.Test
{
    using System;
    using System.Data.Entity;
    using System.ComponentModel.DataAnnotations.Schema;
    using System.Linq;

    public partial class Model1 : DbContext
    {
        public Model1()
            : base("name=Model1")
        {
        }

        public virtual DbSet<location> location { get; set; }
        public virtual DbSet<login_user> login_user { get; set; }
        public virtual DbSet<message> message { get; set; }

        protected override void OnModelCreating(DbModelBuilder modelBuilder)
        {
            modelBuilder.Entity<login_user>()
                .Property(e => e.password)
                .IsFixedLength();
        }
    }
}
