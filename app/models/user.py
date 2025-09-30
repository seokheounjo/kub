from sqlalchemy.orm import Mapped, mapped_column
from sqlalchemy import String, DateTime, func
from .base import Base
from datetime import datetime

class User(Base):
	__tablename__ = "users"
	id: Mapped[int] = mapped_column(primary_key=True)
	email: Mapped[str] = mapped_column(String(255), unique=True, index=True)
	name: Mapped[str] = mapped_column(String(120))
	password_hash: Mapped[str] = mapped_column(String(255))
	created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now())
