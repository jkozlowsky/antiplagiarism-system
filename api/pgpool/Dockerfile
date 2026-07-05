FROM debian:bookworm-slim

RUN apt-get update && apt-get install -y pgpool2 postgresql-client && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /var/run/postgresql && chown -R postgres:postgres /var/run/postgresql

COPY pgpool-custom.conf /tmp/pgpool-custom.conf
RUN cat /tmp/pgpool-custom.conf >> /etc/pgpool2/pgpool.conf

USER postgres

CMD ["pgpool", "-n", "-f", "/etc/pgpool2/pgpool.conf"]