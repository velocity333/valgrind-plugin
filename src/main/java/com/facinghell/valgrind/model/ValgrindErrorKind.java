package com.facinghell.valgrind.model;

public enum ValgrindErrorKind
{
	InvalidRead,
	InvalidWrite,
	Leak_DefinitelyLost,
	Leak_PossiblyLost,
	UninitCondition,
	UninitValue,
}
